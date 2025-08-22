#!/usr/bin/env node

/**
 * Health Check Script for Finance Project Microservices
 *
 * This script checks the health status of all microservices and infrastructure
 * components. It provides detailed status information and can be used for
 * monitoring and debugging purposes.
 */

const http = require('http');
const https = require('https');
const { execSync } = require('child_process');

// ANSI color codes for console output
const colors = {
    reset: '\x1b[0m',
    bright: '\x1b[1m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m'
};

function log(message, color = colors.reset) {
    console.log(`${color}${message}${colors.reset}`);
}

function logSuccess(message) {
    log(`✅ ${message}`, colors.green);
}

function logWarning(message) {
    log(`⚠️  ${message}`, colors.yellow);
}

function logError(message) {
    log(`❌ ${message}`, colors.red);
}

function logInfo(message) {
    log(`ℹ️  ${message}`, colors.blue);
}

function logHeader(message) {
    log(`\n${colors.bright}${colors.cyan}=== ${message} ===${colors.reset}`);
}

/**
 * Service configuration
 */
const services = {
    merchant: {
        name: 'Merchant Service',
        url: 'http://localhost:8083/actuator/health',
        port: 8083
    },
    transaction: {
        name: 'Transaction Service',
        url: 'http://localhost:8082/actuator/health',
        port: 8082
    },
    ledger: {
        name: 'Ledger Service',
        url: 'http://localhost:8080/actuator/health',
        port: 8080
    },
    payout: {
        name: 'Payout Service',
        url: 'http://localhost:8084/actuator/health',
        port: 8084
    }
};

const infrastructure = {
    postgres: {
        name: 'PostgreSQL',
        port: 5432,
        checkCommand: 'docker-compose exec -T postgres pg_isready -U postgres'
    },
    rabbitmq: {
        name: 'RabbitMQ',
        port: 5672,
        managementUrl: 'http://localhost:15672',
        checkCommand: 'docker-compose exec -T rabbitmq rabbitmq-diagnostics -q ping'
    }
};

/**
 * Make HTTP request with timeout
 */
function makeHttpRequest(url, timeout = 5000) {
    return new Promise((resolve, reject) => {
        const urlObj = new URL(url);
        const client = urlObj.protocol === 'https:' ? https : http;

        const req = client.get(url, (res) => {
            let data = '';

            res.on('data', (chunk) => {
                data += chunk;
            });

            res.on('end', () => {
                try {
                    const jsonData = JSON.parse(data);
                    resolve({
                        statusCode: res.statusCode,
                        data: jsonData,
                        headers: res.headers
                    });
                } catch (error) {
                    resolve({
                        statusCode: res.statusCode,
                        data: data,
                        headers: res.headers
                    });
                }
            });
        });

        req.on('error', (error) => {
            reject(error);
        });

        req.setTimeout(timeout, () => {
            req.destroy();
            reject(new Error('Request timeout'));
        });
    });
}

/**
 * Execute command and return result
 */
function executeCommand(command, silent = true) {
    try {
        const result = execSync(command, {
            encoding: 'utf8',
            stdio: silent ? 'pipe' : 'inherit'
        });
        return { success: true, output: result.trim() };
    } catch (error) {
        return { success: false, error: error.message };
    }
}

/**
 * Check if port is open
 */
function checkPort(port, host = 'localhost') {
    return new Promise((resolve) => {
        const net = require('net');
        const socket = new net.Socket();

        socket.setTimeout(100);

        socket.on('connect', () => {
            socket.destroy();
            resolve(true);
        });

        socket.on('timeout', () => {
            socket.destroy();
            resolve(false);
        });

        socket.on('error', () => {
            resolve(false);
        });

        socket.connect(port, host);
    });
}

/**
 * Check health of a single microservice
 */
async function checkServiceHealth(serviceKey, serviceConfig) {
    const { name, url, port } = serviceConfig;

    try {
        // First check if port is open
        const portOpen = await checkPort(port);
        if (!portOpen) {
            logError(`${name} - Port ${port} is not accessible`);
            return { status: 'DOWN', service: name, port, error: 'Port not accessible' };
        }

        // Make health check request
        const response = await makeHttpRequest(url, 100);

        if (response.statusCode === 200) {
            const healthData = response.data;
            if (typeof healthData === 'object' && healthData.status === 'UP') {
                logSuccess(`${name} - Healthy (${url})`);

                // Display additional health details if available
                if (healthData.components) {
                    const components = Object.keys(healthData.components);
                    const healthyComponents = components.filter(comp =>
                        healthData.components[comp].status === 'UP'
                    );
                    const unhealthyComponents = components.filter(comp =>
                        healthData.components[comp].status !== 'UP'
                    );

                    if (unhealthyComponents.length > 0) {
                        logWarning(`  └─ Unhealthy components: ${unhealthyComponents.join(', ')}`);
                    }

                    logInfo(`  └─ Components: ${healthyComponents.length}/${components.length} healthy`);
                }

                return { status: 'UP', service: name, port, details: healthData };
            } else {
                logWarning(`${name} - Service reports DOWN status`);
                return { status: 'DOWN', service: name, port, details: healthData };
            }
        } else {
            logError(`${name} - HTTP ${response.statusCode}`);
            return { status: 'DOWN', service: name, port, error: `HTTP ${response.statusCode}` };
        }
    } catch (error) {
        logError(`${name} - ${error.message}`);
        return { status: 'DOWN', service: name, port, error: error.message };
    }
}

/**
 * Check infrastructure services
 */
async function checkInfrastructure() {
    logHeader('Infrastructure Health Check');

    const results = {};

    // Check PostgreSQL
    const pgResult = executeCommand(infrastructure.postgres.checkCommand);
    if (pgResult.success) {
        logSuccess(`${infrastructure.postgres.name} - Ready`);
        results.postgres = { status: 'UP', service: infrastructure.postgres.name };
    } else {
        logError(`${infrastructure.postgres.name} - Not ready`);
        results.postgres = { status: 'DOWN', service: infrastructure.postgres.name, error: pgResult.error };
    }

    // Check RabbitMQ
    const rabbitResult = executeCommand(infrastructure.rabbitmq.checkCommand);
    if (rabbitResult.success) {
        logSuccess(`${infrastructure.rabbitmq.name} - Ready`);

        // Also check management interface
        try {
            const mgmtResponse = await makeHttpRequest(infrastructure.rabbitmq.managementUrl, 100);
            if (mgmtResponse.statusCode === 200) {
                logInfo(`  └─ Management interface accessible at ${infrastructure.rabbitmq.managementUrl}`);
            }
        } catch (error) {
            logWarning(`  └─ Management interface not accessible`);
        }

        results.rabbitmq = { status: 'UP', service: infrastructure.rabbitmq.name };
    } else {
        logError(`${infrastructure.rabbitmq.name} - Not ready`);
        results.rabbitmq = { status: 'DOWN', service: infrastructure.rabbitmq.name, error: rabbitResult.error };
    }

    return results;
}

/**
 * Check all microservices
 */
async function checkMicroservices() {
    logHeader('Microservices Health Check');

    const results = {};

    for (const [serviceKey, serviceConfig] of Object.entries(services)) {
        results[serviceKey] = await checkServiceHealth(serviceKey, serviceConfig);
    }

    return results;
}

/**
 * Check running processes
 */
function checkRunningProcesses() {
    logHeader('Running Processes Check');

    try {
        const result = executeCommand('ps aux | grep "spring-boot:run" | grep -v grep');
        if (result.success && result.output) {
            const processes = result.output.split('\n').filter(line => line.trim());
            logSuccess(`Found ${processes.length} Spring Boot processes running`);

            processes.forEach((process, index) => {
                const parts = process.split(/\s+/);
                const pid = parts[1];
                const serviceName = process.includes('-pl merchant-service') ? 'merchant-service' :
                                  process.includes('-pl transaction-service') ? 'transaction-service' :
                                  process.includes('-pl ledger-service') ? 'ledger-service' :
                                  process.includes('-pl payout-service') ? 'payout-service' : 'unknown';

                logInfo(`  └─ ${serviceName} (PID: ${pid})`);
            });

            return { processCount: processes.length, processes };
        } else {
            logWarning('No Spring Boot processes found running');
            return { processCount: 0, processes: [] };
        }
    } catch (error) {
        logError(`Failed to check running processes: ${error.message}`);
        return { processCount: 0, processes: [], error: error.message };
    }
}

/**
 * Generate summary report
 */
function generateSummary(infrastructureResults, microserviceResults, processResults) {
    logHeader('Health Check Summary');

    const allResults = { ...infrastructureResults, ...microserviceResults };
    const totalServices = Object.keys(allResults).length;
    const healthyServices = Object.values(allResults).filter(result => result.status === 'UP').length;
    const unhealthyServices = totalServices - healthyServices;

    if (unhealthyServices === 0) {
        logSuccess(`All ${totalServices} services are healthy! 🎉`);
    } else {
        logWarning(`${healthyServices}/${totalServices} services are healthy`);
        logError(`${unhealthyServices} service(s) need attention`);
    }

    // List unhealthy services
    const unhealthy = Object.entries(allResults)
        .filter(([key, result]) => result.status === 'DOWN')
        .map(([key, result]) => result.service);

    if (unhealthy.length > 0) {
        log('\n🔧 Services needing attention:');
        unhealthy.forEach(service => {
            log(`   • ${service}`);
        });
    }

    // Process summary
    if (processResults.processCount > 0) {
        logInfo(`${processResults.processCount} Spring Boot processes are running`);
    } else {
        logWarning('No Spring Boot processes detected');
    }

    return {
        total: totalServices,
        healthy: healthyServices,
        unhealthy: unhealthyServices,
        processCount: processResults.processCount,
        allHealthy: unhealthyServices === 0
    };
}

/**
 * Main health check function
 */
async function main() {
    log(`${colors.bright}${colors.magenta}🏥 Finance Project Health Check${colors.reset}\n`);

    try {
        // Check infrastructure first
        const infrastructureResults = await checkInfrastructure();

        // Check microservices
        const microserviceResults = await checkMicroservices();

        // Check running processes
        const processResults = checkRunningProcesses();

        // Generate summary
        const summary = generateSummary(infrastructureResults, microserviceResults, processResults);

        // Exit with appropriate code
        if (summary.allHealthy && summary.processCount > 0) {
            process.exit(0);
        } else {
            process.exit(1);
        }

    } catch (error) {
        logError(`Health check failed: ${error.message}`);
        process.exit(1);
    }
}

// Export functions for use in other scripts
module.exports = {
    checkServiceHealth,
    checkInfrastructure,
    checkMicroservices,
    checkRunningProcesses,
    makeHttpRequest,
    checkPort,
    services,
    infrastructure
};

// Run the health check if this script is executed directly
if (require.main === module) {
    main();
}

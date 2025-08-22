#!/usr/bin/env node

/**
 * Wait for Service Script for Finance Project
 *
 * This script waits for specific services to become available before proceeding.
 * It's used to ensure proper service startup order and dependency resolution.
 * Supports waiting for both infrastructure services and microservices.
 */

const http = require('http');
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

function logWaiting(message) {
    process.stdout.write(`${colors.yellow}⏳ ${message}${colors.reset}`);
}

/**
 * Service definitions with their health check configurations
 */
const serviceDefinitions = {
    // Infrastructure services
    postgres: {
        name: 'PostgreSQL',
        type: 'infrastructure',
        checkCommand: 'docker-compose exec -T postgres pg_isready -U postgres',
        port: 5432,
        timeout: 60000
    },
    rabbitmq: {
        name: 'RabbitMQ',
        type: 'infrastructure',
        checkCommand: 'docker-compose exec -T rabbitmq rabbitmq-diagnostics -q ping',
        port: 5672,
        timeout: 60000
    },

    // Microservices
    merchant: {
        name: 'Merchant Service',
        type: 'microservice',
        url: 'http://localhost:8083/actuator/health',
        port: 8083,
        timeout: 120000
    },
    transaction: {
        name: 'Transaction Service',
        type: 'microservice',
        url: 'http://localhost:8082/actuator/health',
        port: 8082,
        timeout: 120000
    },
    ledger: {
        name: 'Ledger Service',
        type: 'microservice',
        url: 'http://localhost:8080/actuator/health',
        port: 8080,
        timeout: 120000
    },
    payout: {
        name: 'Payout Service',
        type: 'microservice',
        url: 'http://localhost:8084/actuator/health',
        port: 8084,
        timeout: 120000
    }
};

/**
 * Service dependency order
 */
const serviceDependencies = {
    merchant: ['postgres', 'rabbitmq'],
    transaction: ['postgres', 'rabbitmq', 'merchant'],
    ledger: ['postgres', 'rabbitmq', 'merchant', 'transaction'],
    payout: ['postgres', 'rabbitmq', 'merchant', 'ledger']
};

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

        socket.setTimeout(1000);

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
 * Make HTTP request to health endpoint
 */
function makeHealthRequest(url) {
    return new Promise((resolve) => {
        const req = http.get(url, (res) => {
            let data = '';

            res.on('data', (chunk) => {
                data += chunk;
            });

            res.on('end', () => {
                try {
                    const jsonData = JSON.parse(data);
                    resolve({
                        success: res.statusCode === 200,
                        status: jsonData.status,
                        data: jsonData
                    });
                } catch (error) {
                    resolve({ success: false, error: 'Invalid JSON response' });
                }
            });
        });

        req.on('error', () => {
            resolve({ success: false, error: 'Connection failed' });
        });

        req.setTimeout(3000, () => {
            req.destroy();
            resolve({ success: false, error: 'Request timeout' });
        });
    });
}

/**
 * Wait for infrastructure service to be ready
 */
async function waitForInfrastructure(serviceName, config) {
    const startTime = Date.now();
    const { name, checkCommand, timeout } = config;

    logWaiting(`Waiting for ${name} to be ready`);

    while (Date.now() - startTime < timeout) {
        const result = executeCommand(checkCommand);

        if (result.success) {
            process.stdout.write('\n');
            logSuccess(`${name} is ready`);
            return true;
        }

        process.stdout.write('.');
    }

    process.stdout.write('\n');
    logError(`${name} failed to become ready within ${timeout / 1000}s`);
    return false;
}

/**
 * Wait for microservice to be ready
 */
async function waitForMicroservice(serviceName, config) {
    const startTime = Date.now();
    const { name, url, port, timeout } = config;

    logWaiting(`Waiting for ${name} to be ready`);

    while (Date.now() - startTime < timeout) {
        // First check if port is open
        const portOpen = await checkPort(port);

        if (portOpen) {
            // Then check health endpoint
            const healthResult = await makeHealthRequest(url);

            if (healthResult.success && healthResult.status === 'UP') {
                process.stdout.write('\n');
                logSuccess(`${name} is ready and healthy`);
                return true;
            }
        }

        process.stdout.write('.');
    }

    process.stdout.write('\n');
    logError(`${name} failed to become ready within ${timeout / 1000}s`);
    return false;
}

/**
 * Wait for a single service
 */
async function waitForService(serviceName) {
    const config = serviceDefinitions[serviceName];

    if (!config) {
        logError(`Unknown service: ${serviceName}`);
        return false;
    }

    if (config.type === 'infrastructure') {
        return await waitForInfrastructure(serviceName, config);
    } else if (config.type === 'microservice') {
        return await waitForMicroservice(serviceName, config);
    } else {
        logError(`Unknown service type: ${config.type}`);
        return false;
    }
}

/**
 * Wait for multiple services in dependency order
 */
async function waitForServices(serviceNames) {
    const allServices = new Set(serviceNames);

    // Add dependencies for each requested service
    for (const serviceName of serviceNames) {
        if (serviceDependencies[serviceName]) {
            serviceDependencies[serviceName].forEach(dep => allServices.add(dep));
        }
    }

    // Convert to array and sort by dependency order
    const sortedServices = Array.from(allServices).sort((a, b) => {
        const aIsInfra = serviceDefinitions[a]?.type === 'infrastructure';
        const bIsInfra = serviceDefinitions[b]?.type === 'infrastructure';

        // Infrastructure services first
        if (aIsInfra && !bIsInfra) return -1;
        if (!aIsInfra && bIsInfra) return 1;

        // Then by dependency order
        const aIndex = Object.keys(serviceDependencies).indexOf(a);
        const bIndex = Object.keys(serviceDependencies).indexOf(b);

        if (aIndex === -1 && bIndex === -1) return 0;
        if (aIndex === -1) return -1;
        if (bIndex === -1) return 1;

        return aIndex - bIndex;
    });

    logInfo(`Waiting for services in order: ${sortedServices.join(' → ')}`);

    for (const serviceName of sortedServices) {
        const success = await waitForService(serviceName);
        if (!success) {
            logError(`Failed to wait for ${serviceName}. Stopping.`);
            return false;
        }
    }

    logSuccess('All requested services are ready!');
    return true;
}

/**
 * Parse command line arguments
 */
function parseArguments() {
    const args = process.argv.slice(2);

    if (args.length === 0) {
        logError('No services specified');
        logInfo('Usage: node wait-for-service.js <service1> [service2] ...');
        logInfo('Available services: ' + Object.keys(serviceDefinitions).join(', '));
        process.exit(1);
    }

    // Validate service names
    const invalidServices = args.filter(service => !serviceDefinitions[service]);
    if (invalidServices.length > 0) {
        logError(`Unknown services: ${invalidServices.join(', ')}`);
        logInfo('Available services: ' + Object.keys(serviceDefinitions).join(', '));
        process.exit(1);
    }

    return args;
}

/**
 * Main function
 */
async function main() {
    const services = parseArguments();

    log(`${colors.bright}${colors.cyan}⏳ Waiting for Services${colors.reset}\n`);

    try {
        const success = await waitForServices(services);

        if (success) {
            logSuccess('All services are ready! 🎉');
            process.exit(0);
        } else {
            logError('Some services failed to become ready');
            process.exit(1);
        }
    } catch (error) {
        logError(`Wait operation failed: ${error.message}`);
        process.exit(1);
    }
}

// Export functions for use in other scripts
module.exports = {
    waitForService,
    waitForServices,
    waitForInfrastructure,
    waitForMicroservice,
    serviceDefinitions,
    serviceDependencies,
    checkPort,
    makeHealthRequest
};

// Run if this script is executed directly
if (require.main === module) {
    main();
}

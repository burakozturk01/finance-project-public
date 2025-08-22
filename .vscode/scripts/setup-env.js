#!/usr/bin/env node

/**
 * Environment Setup Script for Finance Project
 *
 * This script validates and sets up the environment for running the microservices.
 * It checks for required environment variables, validates Docker services,
 * and ensures the workspace is ready for development.
 */

const fs = require('fs');
const path = require('path');
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
 * Check if a command exists in the system
 */
function commandExists(command) {
    try {
        execSync(`which ${command}`, { stdio: 'ignore' });
        return true;
    } catch (error) {
        return false;
    }
}

/**
 * Execute a command and return the output
 */
function executeCommand(command, options = {}) {
    try {
        const result = execSync(command, {
            encoding: 'utf8',
            stdio: options.silent ? 'pipe' : 'inherit',
            ...options
        });
        return { success: true, output: result };
    } catch (error) {
        return { success: false, error: error.message, output: error.stdout };
    }
}

/**
 * Check if Docker is running
 */
function checkDockerStatus() {
    logHeader('Checking Docker Status');

    if (!commandExists('docker')) {
        logError('Docker is not installed or not in PATH');
        return false;
    }

    const result = executeCommand('docker info', { silent: true });
    if (!result.success) {
        logError('Docker is not running. Please start Docker Desktop.');
        return false;
    }

    logSuccess('Docker is running');
    return true;
}

/**
 * Check if required environment file exists
 */
function checkEnvironmentFile() {
    logHeader('Checking Environment Configuration');

    const envPath = path.join(process.cwd(), '.env');
    if (!fs.existsSync(envPath)) {
        logError('.env file not found in project root');
        logInfo('Please create a .env file with the required environment variables');
        return false;
    }

    logSuccess('.env file found');

    // Read and validate environment variables
    const envContent = fs.readFileSync(envPath, 'utf8');
    const requiredVars = [
        'SPRING_DATASOURCE_URL',
        'SPRING_DATASOURCE_USERNAME',
        'SPRING_DATASOURCE_PASSWORD',
        'SPRING_RABBITMQ_HOST',
        'SPRING_RABBITMQ_PORT',
        'MERCHANT_SERVER_PORT',
        'TRANSACTION_SERVER_PORT',
        'LEDGER_SERVER_PORT',
        'PAYOUT_SERVER_PORT'
    ];

    const missingVars = [];
    requiredVars.forEach(varName => {
        if (!envContent.includes(varName)) {
            missingVars.push(varName);
        }
    });

    if (missingVars.length > 0) {
        logWarning(`Missing environment variables: ${missingVars.join(', ')}`);
        return false;
    }

    logSuccess('All required environment variables are present');
    return true;
}

/**
 * Check if Maven is available
 */
function checkMaven() {
    logHeader('Checking Maven Installation');

    if (!commandExists('mvn')) {
        logError('Maven is not installed or not in PATH');
        return false;
    }

    const result = executeCommand('mvn --version', { silent: true });
    if (!result.success) {
        logError('Maven is not working properly');
        return false;
    }

    logSuccess('Maven is available');
    return true;
}

/**
 * Check if Java is available
 */
function checkJava() {
    logHeader('Checking Java Installation');

    if (!commandExists('java')) {
        logError('Java is not installed or not in PATH');
        return false;
    }

    const result = executeCommand('java --version', { silent: true });
    if (!result.success) {
        logError('Java is not working properly');
        return false;
    }

    logSuccess('Java is available');
    return true;
}

/**
 * Start Docker Compose services
 */
function startDockerServices() {
    logHeader('Starting Docker Infrastructure Services');

    logInfo('Starting PostgreSQL and RabbitMQ...');
    const result = executeCommand('docker-compose up -d');

    if (!result.success) {
        logError('Failed to start Docker services');
        return false;
    }

    logSuccess('Docker services started successfully');
    return true;
}

/**
 * Wait for Docker services to be ready
 */
function waitForDockerServices() {
    logHeader('Waiting for Docker Services to be Ready');

    logInfo('Waiting for PostgreSQL to be ready...');
    let attempts = 0;
    const maxAttempts = 30;

    while (attempts < maxAttempts) {
        const result = executeCommand('docker-compose exec -T postgres pg_isready -U postgres', { silent: true });
        if (result.success) {
            logSuccess('PostgreSQL is ready');
            break;
        }

        attempts++;
        if (attempts >= maxAttempts) {
            logError('PostgreSQL failed to start within timeout');
            return false;
        }

        process.stdout.write('.');
    }

    logInfo('Waiting for RabbitMQ to be ready...');
    attempts = 0;

    while (attempts < maxAttempts) {
        const result = executeCommand('docker-compose exec -T rabbitmq rabbitmq-diagnostics -q ping', { silent: true });
        if (result.success) {
            logSuccess('RabbitMQ is ready');
            break;
        }

        attempts++;
        if (attempts >= maxAttempts) {
            logError('RabbitMQ failed to start within timeout');
            return false;
        }

        process.stdout.write('.');
    }

    return true;
}

/**
 * Build the common module
 */
function buildCommonModule() {
    logHeader('Building Common Module');

    logInfo('Building common module...');
    const result = executeCommand('mvn clean install -f common/pom.xml -q');

    if (!result.success) {
        logError('Failed to build common module');
        return false;
    }

    logSuccess('Common module built successfully');
    return true;
}

/**
 * Main setup function
 */
function main() {
    log(`${colors.bright}${colors.magenta}🚀 Finance Project Environment Setup${colors.reset}\n`);

    const checks = [
        checkJava,
        checkMaven,
        checkDockerStatus,
        checkEnvironmentFile
    ];

    // Run all checks first
    for (const check of checks) {
        if (!check()) {
            logError('Environment setup failed. Please fix the issues above and try again.');
            process.exit(1);
        }
    }

    // If all checks pass, proceed with setup
    const setupSteps = [
        startDockerServices,
        waitForDockerServices,
        buildCommonModule
    ];

    for (const step of setupSteps) {
        if (!step()) {
            logError('Environment setup failed during execution. Please check the logs above.');
            process.exit(1);
        }
    }

    logHeader('Environment Setup Complete');
    logSuccess('All systems are ready for development!');
    logInfo('You can now start the microservices using VSCode launch configurations.');

    // Display service URLs
    log('\n📋 Service URLs:');
    log('   • Merchant Service:    http://localhost:8083');
    log('   • Transaction Service: http://localhost:8082');
    log('   • Ledger Service:      http://localhost:8080');
    log('   • Payout Service:      http://localhost:8084');
    log('   • PostgreSQL:          localhost:5432');
    log('   • RabbitMQ Management: http://localhost:15672');
}

// Run the setup if this script is executed directly
if (require.main === module) {
    main();
}

module.exports = {
    checkDockerStatus,
    checkEnvironmentFile,
    checkMaven,
    checkJava,
    startDockerServices,
    waitForDockerServices,
    buildCommonModule
};

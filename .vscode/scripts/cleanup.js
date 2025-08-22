#!/usr/bin/env node

/**
 * Cleanup Script for Finance Project
 *
 * This script provides various cleanup operations for the development environment.
 * It can stop running services, clean build artifacts, reset Docker containers,
 * and restore the workspace to a clean state.
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
 * Execute command and return result
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
 * Check if command exists
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
 * Stop all Spring Boot processes
 */
function stopSpringBootProcesses() {
    logHeader('Stopping Spring Boot Processes');

    try {
        // Find all Spring Boot processes
        const result = executeCommand('ps aux | grep "spring-boot:run" | grep -v grep', { silent: true });

        if (!result.success || !result.output.trim()) {
            logInfo('No Spring Boot processes found running');
            return true;
        }

        const processes = result.output.split('\n').filter(line => line.trim());
        logInfo(`Found ${processes.length} Spring Boot processes`);

        // Extract PIDs and kill processes
        const pids = [];
        processes.forEach(process => {
            const parts = process.split(/\s+/);
            const pid = parts[1];
            const serviceName = process.includes('-pl merchant-service') ? 'merchant-service' :
                              process.includes('-pl transaction-service') ? 'transaction-service' :
                              process.includes('-pl ledger-service') ? 'ledger-service' :
                              process.includes('-pl payout-service') ? 'payout-service' : 'unknown';

            pids.push({ pid, service: serviceName });
        });

        // Kill processes gracefully first (SIGTERM)
        logInfo('Sending SIGTERM to processes...');
        pids.forEach(({ pid, service }) => {
            try {
                execSync(`kill ${pid}`, { stdio: 'ignore' });
                logInfo(`  └─ Sent SIGTERM to ${service} (PID: ${pid})`);
            } catch (error) {
                logWarning(`  └─ Failed to send SIGTERM to ${service} (PID: ${pid})`);
            }
        });

        // Wait a bit for graceful shutdown
        logInfo('Waiting for graceful shutdown...');

        // Check if any processes are still running and force kill if necessary
        const stillRunning = executeCommand('ps aux | grep "spring-boot:run" | grep -v grep', { silent: true });
        if (stillRunning.success && stillRunning.output.trim()) {
            logWarning('Some processes still running, force killing...');
            const remainingProcesses = stillRunning.output.split('\n').filter(line => line.trim());

            remainingProcesses.forEach(process => {
                const parts = process.split(/\s+/);
                const pid = parts[1];
                try {
                    execSync(`kill -9 ${pid}`, { stdio: 'ignore' });
                    logInfo(`  └─ Force killed PID: ${pid}`);
                } catch (error) {
                    logWarning(`  └─ Failed to force kill PID: ${pid}`);
                }
            });
        }

        logSuccess('All Spring Boot processes stopped');
        return true;

    } catch (error) {
        logError(`Failed to stop Spring Boot processes: ${error.message}`);
        return false;
    }
}

/**
 * Stop Docker Compose services
 */
function stopDockerServices() {
    logHeader('Stopping Docker Services');

    if (!commandExists('docker-compose')) {
        logWarning('Docker Compose not found, skipping Docker cleanup');
        return true;
    }

    try {
        logInfo('Stopping Docker Compose services...');
        const result = executeCommand('docker-compose down');

        if (result.success) {
            logSuccess('Docker services stopped');
            return true;
        } else {
            logError('Failed to stop Docker services');
            return false;
        }
    } catch (error) {
        logError(`Docker cleanup failed: ${error.message}`);
        return false;
    }
}

/**
 * Clean Maven build artifacts
 */
function cleanMavenArtifacts() {
    logHeader('Cleaning Maven Build Artifacts');

    if (!commandExists('mvn')) {
        logWarning('Maven not found, skipping Maven cleanup');
        return true;
    }

    try {
        logInfo('Running Maven clean...');
        const result = executeCommand('mvn clean -q');

        if (result.success) {
            logSuccess('Maven artifacts cleaned');

            // Also remove any remaining target directories
            const targetDirs = [
                'target',
                'common/target',
                'merchant-service/target',
                'transaction-service/target',
                'ledger-service/target',
                'payout-service/target'
            ];

            targetDirs.forEach(dir => {
                if (fs.existsSync(dir)) {
                    try {
                        fs.rmSync(dir, { recursive: true, force: true });
                        logInfo(`  └─ Removed ${dir}`);
                    } catch (error) {
                        logWarning(`  └─ Failed to remove ${dir}: ${error.message}`);
                    }
                }
            });

            return true;
        } else {
            logError('Maven clean failed');
            return false;
        }
    } catch (error) {
        logError(`Maven cleanup failed: ${error.message}`);
        return false;
    }
}

/**
 * Clean Docker volumes and networks (optional deep clean)
 */
function cleanDockerResources(deep = false) {
    if (!deep) return true;

    logHeader('Deep Cleaning Docker Resources');

    if (!commandExists('docker')) {
        logWarning('Docker not found, skipping Docker resource cleanup');
        return true;
    }

    try {
        // Remove unused volumes
        logInfo('Removing unused Docker volumes...');
        executeCommand('docker volume prune -f', { silent: true });

        // Remove unused networks
        logInfo('Removing unused Docker networks...');
        executeCommand('docker network prune -f', { silent: true });

        // Remove unused images (be careful with this)
        logInfo('Removing unused Docker images...');
        executeCommand('docker image prune -f', { silent: true });

        logSuccess('Docker resources cleaned');
        return true;

    } catch (error) {
        logError(`Docker resource cleanup failed: ${error.message}`);
        return false;
    }
}

/**
 * Clean IDE and temporary files
 */
function cleanTempFiles() {
    logHeader('Cleaning Temporary Files');

    const tempPatterns = [
        '**/.DS_Store',
        '**/Thumbs.db',
        '**/*.log',
        '**/*.tmp',
        '**/node_modules/.cache',
        '**/.vscode/.ropeproject'
    ];

    let cleanedCount = 0;

    // Clean .DS_Store files (macOS)
    try {
        const result = executeCommand('find . -name ".DS_Store" -type f', { silent: true });
        if (result.success && result.output.trim()) {
            const files = result.output.split('\n').filter(f => f.trim());
            files.forEach(file => {
                try {
                    fs.unlinkSync(file);
                    cleanedCount++;
                } catch (error) {
                    // Ignore errors
                }
            });
        }
    } catch (error) {
        // Ignore errors
    }

    // Clean log files in project
    try {
        const result = executeCommand('find . -name "*.log" -type f -not -path "./node_modules/*"', { silent: true });
        if (result.success && result.output.trim()) {
            const files = result.output.split('\n').filter(f => f.trim());
            files.forEach(file => {
                try {
                    fs.unlinkSync(file);
                    cleanedCount++;
                } catch (error) {
                    // Ignore errors
                }
            });
        }
    } catch (error) {
        // Ignore errors
    }

    if (cleanedCount > 0) {
        logSuccess(`Cleaned ${cleanedCount} temporary files`);
    } else {
        logInfo('No temporary files found to clean');
    }

    return true;
}

/**
 * Reset workspace to clean state
 */
function resetWorkspace() {
    logHeader('Resetting Workspace');

    // Kill any remaining Java processes related to the project
    try {
        const javaProcesses = executeCommand('ps aux | grep java | grep finance-project | grep -v grep', { silent: true });
        if (javaProcesses.success && javaProcesses.output.trim()) {
            logInfo('Killing remaining Java processes...');
            const processes = javaProcesses.output.split('\n').filter(line => line.trim());
            processes.forEach(process => {
                const parts = process.split(/\s+/);
                const pid = parts[1];
                try {
                    execSync(`kill -9 ${pid}`, { stdio: 'ignore' });
                } catch (error) {
                    // Ignore errors
                }
            });
        }
    } catch (error) {
        // Ignore errors
    }

    logSuccess('Workspace reset complete');
    return true;
}

/**
 * Display cleanup summary
 */
function displaySummary(operations) {
    logHeader('Cleanup Summary');

    const successful = operations.filter(op => op.success).length;
    const failed = operations.length - successful;

    if (failed === 0) {
        logSuccess(`All ${operations.length} cleanup operations completed successfully! 🎉`);
    } else {
        logWarning(`${successful}/${operations.length} cleanup operations completed`);
        if (failed > 0) {
            logError(`${failed} operation(s) failed`);
        }
    }

    // List failed operations
    const failedOps = operations.filter(op => !op.success);
    if (failedOps.length > 0) {
        log('\n🔧 Failed operations:');
        failedOps.forEach(op => {
            log(`   • ${op.name}`);
        });
    }

    logInfo('\n📋 Workspace is now clean and ready for development');
}

/**
 * Parse command line arguments
 */
function parseArguments() {
    const args = process.argv.slice(2);

    const options = {
        processes: true,
        docker: true,
        maven: true,
        temp: true,
        deep: false,
        all: false
    };

    // Parse flags
    args.forEach(arg => {
        switch (arg) {
            case '--deep':
                options.deep = true;
                break;
            case '--all':
                options.all = true;
                options.deep = true;
                break;
            case '--processes-only':
                options.docker = false;
                options.maven = false;
                options.temp = false;
                break;
            case '--docker-only':
                options.processes = false;
                options.maven = false;
                options.temp = false;
                break;
            case '--maven-only':
                options.processes = false;
                options.docker = false;
                options.temp = false;
                break;
            case '--help':
                log('Finance Project Cleanup Script\n');
                log('Usage: node cleanup.js [options]\n');
                log('Options:');
                log('  --deep           Deep clean including Docker resources');
                log('  --all            Complete cleanup (includes --deep)');
                log('  --processes-only Only stop running processes');
                log('  --docker-only    Only stop Docker services');
                log('  --maven-only     Only clean Maven artifacts');
                log('  --help           Show this help message');
                process.exit(0);
                break;
        }
    });

    return options;
}

/**
 * Main cleanup function
 */
async function main() {
    const options = parseArguments();

    log(`${colors.bright}${colors.magenta}🧹 Finance Project Cleanup${colors.reset}\n`);

    const operations = [];

    try {
        // Stop processes
        if (options.processes) {
            const result = stopSpringBootProcesses();
            operations.push({ name: 'Stop Spring Boot Processes', success: result });
        }

        // Stop Docker services
        if (options.docker) {
            const result = stopDockerServices();
            operations.push({ name: 'Stop Docker Services', success: result });
        }

        // Clean Maven artifacts
        if (options.maven) {
            const result = cleanMavenArtifacts();
            operations.push({ name: 'Clean Maven Artifacts', success: result });
        }

        // Clean temporary files
        if (options.temp) {
            const result = cleanTempFiles();
            operations.push({ name: 'Clean Temporary Files', success: result });
        }

        // Deep clean Docker resources
        if (options.deep) {
            const result = cleanDockerResources(true);
            operations.push({ name: 'Deep Clean Docker Resources', success: result });
        }

        // Reset workspace
        const resetResult = resetWorkspace();
        operations.push({ name: 'Reset Workspace', success: resetResult });

        // Display summary
        displaySummary(operations);

        // Exit with appropriate code
        const allSuccessful = operations.every(op => op.success);
        process.exit(allSuccessful ? 0 : 1);

    } catch (error) {
        logError(`Cleanup failed: ${error.message}`);
        process.exit(1);
    }
}

// Export functions for use in other scripts
module.exports = {
    stopSpringBootProcesses,
    stopDockerServices,
    cleanMavenArtifacts,
    cleanDockerResources,
    cleanTempFiles,
    resetWorkspace
};

// Run if this script is executed directly
if (require.main === module) {
    main();
}

export class Logger {
    static Level = {
	DEBUG:7,
	INFO:6,
	ERROR:3
    }
    level = Logger.Level.INFO

    constructor(level) {
	this.level = level
    }

    logInfo(message) {
	if (this.level >= Logger.Level.INFO)
	    this.log("[INFO]", message)
    }

    logDebug(message) {
	if (this.level >= Logger.Level.DEBUG)
	    this.log("[DEBUG]", message)
    }
    
    logError(message) {
	if (this.level >= Logger.Level.ERROR)
	    this.logToError("[ERROR]", message)
    }

    log(level, message) {
	console.log(`${level} at ${new Date()}: ${message}`)
    }
    
    logToError(level, message) {
	console.error(`${level} at ${new Date()}: ${message}`)
    }

    logAtLevel(level) {
	this.level=level
    }
}

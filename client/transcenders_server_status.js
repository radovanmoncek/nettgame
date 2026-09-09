export class ServerStatus {
    static OFFLINE=1
    static ONLINE=2

    static convertToString(status) {
	switch (status) {
	    case ServerStatus.ONLINE:
		return "Online"

		break

	    case ServerStatus.OFFLINE:
		return "Offline"

		break
	}

	return "Unknown server status"
    }
}

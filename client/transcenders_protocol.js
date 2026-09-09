export const MAX_NAME_LENGTH = 8
export const MAX_TRANSFER_BUFFER_LENGTH=32 

export const Protocol = {
    Reserved: {
	pingPong: 0x1,
	joinNew: 0x2,
	joinExisting: 0x3,
	error: 0x4,
	deleteRequest: 0x5
    },
    Usable: {
	player: 0x9,
	enemyThreat: 0xA,
	weapon: 0xB,
	item: 0xC
    }
}

import { Protocol, MAX_NAME_LENGTH, MAX_TRANSFER_BUFFER_LENGTH } from './transcenders_protocol.js'
import { Player } from './transcenders_player.js'
import { Item } from './transcenders_item.js'
import { Entity } from './transcenders_entity.js'
import { EnemyThreat } from './transcenders_enemy_threat.js'
import { Logger } from './transcenders_logger.js'

export class Codec {
    logger = new Logger()

    constructor() {
	this.logger.logAtLevel(Logger.Level.INFO)
    }

    multiplexEncode(protocolId, input) {
	const view = new DataView(new ArrayBuffer(MAX_TRANSFER_BUFFER_LENGTH))
	const encodeString = (input, view=new DataView(new ArrayBuffer(input.length)), offset=0) => {
	    for (let i = 0; i < input.length; ++i) {
		view.setUint8(offset+i, input.charCodeAt(i), true)
	    }

	    return view.buffer
	}

	let offset = 0

	switch(protocolId) {
	    case Protocol.Reserved.pingPong:
		view.setUint8(offset, Protocol.Reserved.pingPong, true)

		return view.buffer

	    case Protocol.Reserved.joinNew:
		view.setUint8(offset++, protocolId, true);

		return view.buffer;

	    case Protocol.Reserved.joinExisting:
		view.setUint8(offset++,protocolId, true)
		encodeString(input, view, offset);

		offset += 4; // magic

		return view.buffer

	    case Protocol.Usable.player:
		view.setUint8(offset++, protocolId, true)
		view.setUint8(offset++, input.uniqueId, true)
		view.setFloat32(offset, input.position.x, true)

		offset+=4

		view.setFloat32(offset, input.position.y, true)

		offset+=4

		view.setFloat32(offset, input.position.z, true)

		offset+=4

		view.setFloat32(offset, input.azimuth, true)

		offset+=4

		view.setUint8(offset++, input.firing, true)

		encodeString(input.name, view, offset)

		return view.buffer
	}
    }

    multiplexDecode(input) {
	const view = new DataView(input)

	function decodeString(inputView, size, offset=0) {
	    let output = ""

	    for (let i = offset; i < size+offset; ++i) {
		output=output.concat(String.fromCharCode(inputView.getUint8(i, true)))
	    }

	    this.logger.logDebug(`decoded ${output}`)

	    return output
	}

	decodeString = decodeString.bind(this)

	let offset = 0

	const protocolId = view.getUint8(offset++, true)

	switch (protocolId) {
	    case Protocol.Reserved.pingPong:
		return Protocol.Reserved.pingPong;

	    case Protocol.Reserved.joinNew:
		return decodeString(view, 4, offset) // magic, offset += 4

	    case Protocol.Reserved.joinExisting:
	    case Protocol.Reserved.deleteRequest:
		return view.getUint8(offset++, true)

	    case Protocol.Usable.player:
		const player = new Player()

		player.uniqueId=view.getUint8(offset++, true)
		player.position.x = view.getFloat32(offset, true)
		offset+=4
		player.position.y=view.getFloat32(offset, true)
		offset+=4
		player.position.z=view.getFloat32(offset, true)
		offset+=4
		player.azimuth=view.getFloat32(offset, true)
		offset+=4
		player.name=decodeString(view, MAX_NAME_LENGTH, offset)

		return player

	    case Protocol.Usable.enemyThreat:
		{
		const enemy = new EnemyThreat()

		enemy.uniqueId=view.getUint8(offset++, true)
		enemy.position.x = view.getFloat32(offset, true)
		offset+=4
		enemy.position.y=view.getFloat32(offset, true)
		offset+=4
		enemy.position.z=view.getFloat32(offset, true)
		offset+=4
		enemy.azimuth=view.getFloat32(offset, true)
		offset+=4

		return enemy
		}
	    
	    case Protocol.Usable.item:
		{
		const item = new Item()

		item.uniqueId=view.getUint8(offset++, true)
		item.position.x = view.getFloat32(offset, true)
		offset+=4
		item.position.y=view.getFloat32(offset, true)
		offset+=4
		item.position.z=view.getFloat32(offset, true)
		offset+=4

		return item
		}
	}
    }
}

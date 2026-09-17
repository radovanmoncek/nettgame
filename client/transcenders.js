import { Logger } from './transcenders_logger.js'
import { Codec } from './transcenders_codec.js'
import { Protocol, MAX_NAME_LENGTH } from './transcenders_protocol.js'
import { Entity } from './transcenders_entity.js'
import { Item } from './transcenders_item.js'
import { Player } from './transcenders_player.js'
import { EnemyThreat } from './transcenders_enemy_threat.js'
import { ServerStatus } from './transcenders_server_status.js'

/**
 * Synopsis:
 *
 * This class represents a simple game client for the Transcenders multiplayer game that serves as a tool of demonstration for Nettgame.
 *
 * Attributions:
 *
 * source: https://developer.mozilla.org/en-US
 *
 * author: Radovan Moncek
 */
class Game {
    static colourVertexShader = 
	`
	    attribute vec3 position; 
	    attribute vec4 color;

	    uniform mat4 model; 
	    uniform mat4 view; 
	    uniform mat4 projection; 

	    varying vec4 vColor; 

	    void main() {
		vColor = color; 
		gl_Position = projection * view * model * vec4(position, 1.0);
	    }
	`
    static colourFragmentShader = 
    `
	    precision mediump float; 

	    varying vec4 vColor; 

	    void main() {
		gl_FragColor = vColor;
	    }
	    `
    static textureVertexShader =
	`
	attribute vec3 position;
	attribute vec2 textureCoord;

	uniform mat4 model;
	uniform mat4 view;
	uniform mat4 projection;

	varying highp vec2 vTextureCoord;

	void main() {
		gl_Position = projection * view * model * vec4(position, 1.0);
		vTextureCoord = textureCoord;
	}
	`
    static textureFragmentShader =
	`
	varying highp vec2 vTextureCoord;

	uniform sampler2D sampler;

	void main() {
		gl_FragColor = texture2D(sampler, vTextureCoord);
	}
	`
    static BLUE = [0, 0, 1.0, 1.0]
    static GREY = [0.4, 0.4, 0.4, 1.0]
    static pingTimeout = 4000
    static identity = [
	[1, 0, 0, 0],
	[0, 1, 0, 0],
	[0, 0, 1, 0],
	[0, 0, 0, 1]
    ]
    static isometricRotation = Game.multiplyMatrices(
	Game.rotateY(Math.PI/4),
	Game.rotateX(-Math.PI/8)
    )
    static opaqueBlue = [0,0,255,255]

    entities=[]
    locations={}
    transforms={}
    uI = document.createElement("div")
    canvas = document.createElement("canvas")
    html = document.getElementsByTagName("html")[0]
    gl
    webSocket = null
    serverStatus=ServerStatus.OFFLINE
    heldKeys = new Set()
    assignedUniqueId = null
    movementX=0
    speed = 0.05
    mouseSensitivity=0.06
    fps=0
    lobbyCode=null
    logger = new Logger(Logger.Level.INFO)
    codec = new Codec()
    worldWidth=10
    worldHeight=10
    serverTickRate=1000/60
    cameraOffset = -20
    cameraLow = 2
    firing=false
    announcmentShowLength = 2*1000

    static generateColours(colour) {
	const faceColour = [0,0,0,0].map(face => colour).flat()
	
	return new Float32Array([
	    faceColour,
	    faceColour,
	    faceColour,
	    faceColour,
	    faceColour,
	    faceColour
	].flat())
    }

    static async sleep(time) {
	return await new Promise((resolve, reject) => setTimeout(() => resolve(), time))
    }

    static multiplyMatrices(...matrices) {
	const result = []

	function twoWay(a, b) {
	    const result_ = []

	    for (let i = 0; i < a.length; ++i) {
		result_[i] = []

		for (let k = 0; k < b.length; ++k)
		    result_[i][k]=0

		for (let j = 0; j < a[i].length; ++j) {
		    for (let k = 0; k < b.length; ++k) {
			result_[i][k]+=a[i][j]*b[j][k]
		    }
		}
	    }

	    return result_
	}

	return matrices.reduce((result, matrix) => twoWay(result, matrix))
    }

    static scale(x, y, z) {
	return [
	    [x, 0, 0, 0],
	    [0, y, 0, 0],
	    [0, 0, z, 0],
	    [0, 0, 0, 1]
	]
    }

    static translate(x, y, z) {
	return [
	    [1, 0, 0, 0],
	    [0, 1, 0, 0],
	    [0, 0, 1, 0],
	    [x, y, z, 1]
	]
    }

    static rotateX(angle) {
	return [
	    [1, 0, 0, 0],
	    [0, Math.cos(angle), -Math.sin(angle), 0],
	    [0, Math.sin(angle), Math.cos(angle), 0],
	    [0, 0, 0, 1]
	]
    }

    static rotateY(angle) {
	return [
	    [Math.cos(angle), 0, Math.sin(angle), 0],
	    [0, 1, 0, 0],
	    [-Math.sin(angle), 0, Math.cos(angle), 0],
	    [0, 0, 0, 1]
	]
    }

    static rotateZ(anglex) {
	return [
	    [Math.cos(angle), -Math.sin(angle), 0, 0],
	    [Math.sin(angle), Math.cos(angle), 0, 0],
	    [0, 0, 1, 0],
	    [0, 0, 0, 1]
	]
    }

	static createMessage(text) {
	    const message = document.createElement("p")
	    
	    message.innerText=text
	    message.style="align-self:center;"

	    return message
	}

    constructor(addresses, port) {
	this.setupUI()
	this.setupRendering()
	this.startEventLoop()
	this.connectToServer(addresses, port)
	this.setupListeners()
	this.changeSceneMenu()
    }

    /**
     * Attributions:
     *
     * source: https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Tutorial/Using_textures_in_WebGL
     * source: https://hacks.mozilla.org/2011/11/using-cors-to-load-webgl-textures-from-cross-domain-images/
     *
     * author: Radovan Moncek
     */
    loadTexture(uRL) {
	function isPowerOfTwo(n) {
	    return (n&(n-1))===0
	}

	const texture = this.gl.createTexture()
	const level = 0
	const width = 1
	const height = 1
	const border = 0
	const image = new Image()

	this.gl.bindTexture(this.gl.TEXTURE_2D, texture)
	this.gl.texImage2D(
	    this.gl.TEXTURE_2D,
	    level,
	    this.gl.RGBA,
	    width,
	    height,
	    border,
	    this.gl.RGBA,
	    this.gl.UNSIGNED_BYTE,
	    new Uint8Array(Game.opaqueBlue)
	)

	image.onload = () => {
	    this.gl.bindTexture(this.gl.TEXTURE_2D, texture)
	    this.gl.texImage2D(
		this.gl.TEXTURE_2D,
		level,
		this.gl.RGBA,	
		this.gl.RGBA,
		this.gl.UNSIGNED_BYTE,
		image
	    )
	

	if (isPowerOfTwo(image.width) && isPowerOfTwo(image.height)) {
	    this.gl.generateMipmap(this.gl.TEXTURE_2D)
	}
	else {
	    this.gl.texParameteri(this.gl.TEXTURE_2D, this.gl.TEXTURE_WRAP_S, this.gl.CLAMP_TO_EDGE)
	    this.gl.texParameteri(this.gl.TEXTURE_2D, this.gl.TEXTURE_WRAP_T, this.gl.CLAMP_TO_EDGE)
	    this.gl.texParameteri(this.gl.TEXTURE_2D, this.gl.TEXTURE_MIN_FILTER, this.gl.LINEAR)
	}
	}

	image.src=uRL
	image.crossOrigin="anonymous"

	return texture
    }

    setupUI() {
	document.body.style="margin: 0; overflow: hidden; width: 100%; height: 100%;"
	this.html.style="margin: 0; overflow: hidden; width: 100%; height: 100%;"
	this.uI.style="position: absolute; top: 0; width:100%;"
	this.connectionStatistics = document.createElement("div")
	this.connectionStatistics.style = "display:flex; flex-direction:row;"
	this.pingStatistic = document.createElement("p")
	this.serverConnectionStatistic=document.createElement("p")
	this.serverConnectionStatistic.innerText=ServerStatus.convertToString(this.serverStatus)
	this.fpsStatistic=document.createElement("p")
	this.lobbyCodeStatistic=document.createElement("p")
	this.canvas.style="width: 100% !important; height: 100% !important;"
	this.canvas.width=window.innerWidth
	this.canvas.height=window.innerHeight
	this.gl = this.canvas.getContext("webgl")

	this.connectionStatistics.append(this.pingStatistic, this.serverConnectionStatistic, this.fpsStatistic, this.lobbyCodeStatistic)
	this.uI.appendChild(this.connectionStatistics)
	document.body.append(this.uI, this.canvas)
    }

    setupListeners() {
	document.addEventListener("keydown", ({code}) => this.heldKeys.add(code))
	document.addEventListener("keyup", ({code}) => this.heldKeys.delete(code))
	document.addEventListener("mousemove", ({movementX}) => this.movementX=movementX)
	document.addEventListener("click", () => this.firing=true)
    }

    connectToServer(addresses, port) {
	let ping=Date.now()
	let lastPing=Date.now()

	for (let address of addresses)
	    this.webSocket = new WebSocket(`ws://${address}:${port}`)

	this.webSocket.binaryType="arraybuffer"
	this.webSocket.onopen = () => {
	    this.connected=true
	    this.serverStatus=ServerStatus.ONLINE
	    this.serverConnectionStatistic.innerText=ServerStatus.convertToString(this.serverStatus)

	    this.webSocket.send(this.codec.multiplexEncode(Protocol.Reserved.pingPong))
	}
	this.webSocket.onmessage=(event)=>{
	    if (event.data instanceof ArrayBuffer) {
		const arrayBuffer = event.data

		let offset = 0

		if (new DataView(arrayBuffer).getUint8(offset)===Protocol.Reserved.pingPong) {
		    (async ()=>{
			let delta = Date.now()-lastPing

			lastPing=Date.now()

			let ping = Math.max(1, delta-Game.pingTimeout)

			this.pingStatistic.innerText=`${ping}ms`

			await Game.sleep(Game.pingTimeout)

			if (this.webSocket.readyState===WebSocket.OPEN)
			    this.webSocket.send(this.codec.multiplexEncode(Protocol.Reserved.pingPong))
		    })()

		    return
		}

		if (new DataView(arrayBuffer).getUint8(offset)===Protocol.Reserved.joinNew) {
		    this.lobbyCode=this.codec.multiplexDecode(arrayBuffer)
		    this.lobbyCodeStatistic.innerText=`game session ${this.lobbyCode}`

		    this.logger.logInfo(`got aknowledgement of game session creation with code ${this.lobbyCode}`)
		    this.webSocket.send(this.codec.multiplexEncode(Protocol.Reserved.joinExisting, this.lobbyCode))

		    return
		}

		if (!this.assignedUniqueId&&new DataView(arrayBuffer).getUint8(offset)===Protocol.Reserved.joinExisting) {
		    this.assignedUniqueId=this.codec.multiplexDecode(arrayBuffer)

		    this.logger.logInfo(`got aknowledgement of join to game session with unique id ${this.assignedUniqueId}`)
		    this.changeSceneGame()

		    return
		}

		if (new DataView(arrayBuffer).getUint8(offset)===Protocol.Reserved.deleteRequest) {
		    ++offset
		    this.entities = this.entities.filter(entity => {
			if (entity.uniqueId !== new DataView(arrayBuffer).getUint8(offset))
			    return true

			if (entity instanceof Item) {
			    this.announcment.innerText = `picked up ${entity.name}`

			    setTimeout(() => this.announcment.innerText = "", this.announcmentShowLength)
			}

			// add spinning model of the picked up item to a fixed position on screen

			return false
		    })

		    return;
		}

		const decoded = this.codec.multiplexDecode(arrayBuffer)
		const {uniqueId, position, azimuth, name} = decoded
		const {x,y,z}=position

		this.logger.logDebug(`${x} ${y} ${z} ${name}`)

		let foundEntityId = false

		for (let entity of this.entities) {
		    if (entity.uniqueId===uniqueId) {
			foundEntityId=true
			entity.transforms.model = Game.multiplyMatrices(
			    Game.rotateY(azimuth),
			    Game.translate(x, y, z),
			    Game.scale(0.2, 0.4, 0.2),
			    Game.isometricRotation
			)

			if (this.assignedUniqueId===entity.uniqueId) {
			    //camera?
			}

			entity.position=position
			entity.azimuth=azimuth
		    }
		}

		if (!foundEntityId) {
		    decoded.buffers = this.createPlayer()

		    this.entities.push(decoded)

		    decoded.position={x, y, z}

		    switch (new DataView(arrayBuffer).getUint8(offset)) {
			case Protocol.Usable.player:
			case Protocol.Usable.enemyThreat:
			    decoded.transforms.model = Game.multiplyMatrices(
				Game.translate(0, 1, 0),
				Game.scale(0.2, 0.4, 0.2),
				Game.isometricRotation
			    )

			    break;
			case Protocol.Usable.item:
			    decoded.transforms.model = Game.multiplyMatrices(
				Game.translate(x, y, z),
				Game.scale(0.2, 0.2, 0.2),
				Game.isometricRotation
			    )

			    break;
		    }
		    //decoded.buffers.texture = this.loadTexture("https://raw.githubusercontent.com/wwwriks/wrad-textures/refs/heads/main/seamless/concrete1.png")
		}
	    }
	}
	this.webSocket.onerror=()=> {
	    this.logger.logError("web socket encountered an error")
	}
	this.webSocket.onclose=()=> {
	    this.logger.logInfo("web socket closed, will attempt reconnect")

	    this.connectToServer(addresses, port)
	}
    }

    /**
     * Attributions:
     *
     * source: https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Tutorial/Using_textures_in_WebGL
     *
     * author: Radovan Moncek
     */
    createPlayer() {
	const vertices = new Float32Array([
	    -1.0, -1.0, 1.0, 1.0, -1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0, 1.0,
	    -1.0, -1.0, -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, 1.0, -1.0, -1.0,
	    -1.0, 1.0, -1.0, -1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, -1.0,
	    -1.0, -1.0, -1.0, 1.0, -1.0, -1.0, 1.0, -1.0, 1.0, -1.0, -1.0, 1.0,
	    1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0,
	    -1.0, -1.0, -1.0, -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, 1.0, -1.0
	])
	const indices = new Uint16Array([
	    0,  1,  2,   0,  2,  3,
	    4,  5,  6,   4,  6,  7,
	    8,  9,  10,  8,  10, 11,
	    12, 13, 14,  12, 14, 15,
	    16, 17, 18,  16, 18, 19,
	    20, 21, 22,  20, 22, 23
	])
	const colours = new Float32Array([
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0
	])

	const vertices_ = this.gl.createBuffer()

	this.gl.bindBuffer(this.gl.ARRAY_BUFFER, vertices_)
	this.gl.bufferData(this.gl.ARRAY_BUFFER, vertices, this.gl.STATIC_DRAW)

	const indices_ = this.gl.createBuffer()

	this.gl.bindBuffer(this.gl.ELEMENT_ARRAY_BUFFER, indices_)
	this.gl.bufferData(this.gl.ELEMENT_ARRAY_BUFFER, indices, this.gl.STATIC_DRAW)

	const colours_ = this.gl.createBuffer()

	this.gl.bindBuffer(this.gl.ARRAY_BUFFER, colours_)
	this.gl.bufferData(this.gl.ARRAY_BUFFER, colours, this.gl.STATIC_DRAW)

	return {vertices: vertices_, indices: indices_, colours: colours_, triangleCount: indices.length}
    }

    /**
     * Attributions:
     *
     * source: https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Tutorial/Using_textures_in_WebGL
     *
     * author: Radovan Moncek
     */
    createGround() {
	const vertices = new Float32Array([
	    -1.0, -1.0, 1.0, 1.0, -1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0, 1.0,
	    -1.0, -1.0, -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, 1.0, -1.0, -1.0,
	    -1.0, 1.0, -1.0, -1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, -1.0,
	    -1.0, -1.0, -1.0, 1.0, -1.0, -1.0, 1.0, -1.0, 1.0, -1.0, -1.0, 1.0,
	    1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0, 1.0, 1.0, 1.0, -1.0, 1.0,
	    -1.0, -1.0, -1.0, -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, 1.0, -1.0
	])
	const indices = new Uint16Array([
	    0,  1,  2,   0,  2,  3,
	    4,  5,  6,   4,  6,  7,
	    8,  9,  10,  8,  10, 11,
	    12, 13, 14,  12, 14, 15,
	    16, 17, 18,  16, 18, 19,
	    20, 21, 22,  20, 22, 23
	])
	const colours = new Float32Array([
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0,
	    0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0
	])

	const vertices_ = this.gl.createBuffer()

	this.gl.bindBuffer(this.gl.ARRAY_BUFFER, vertices_)
	this.gl.bufferData(this.gl.ARRAY_BUFFER, vertices, this.gl.STATIC_DRAW)

	const indices_ = this.gl.createBuffer()

	this.gl.bindBuffer(this.gl.ELEMENT_ARRAY_BUFFER, indices_)
	this.gl.bufferData(this.gl.ELEMENT_ARRAY_BUFFER, indices, this.gl.STATIC_DRAW)

	const colours_ = this.gl.createBuffer()

	this.gl.bindBuffer(this.gl.ARRAY_BUFFER, colours_)
	this.gl.bufferData(this.gl.ARRAY_BUFFER, colours, this.gl.STATIC_DRAW)

	return {vertices: vertices_, indices: indices_, colours: colours_, triangleCount: indices.length}
    }

    /**
    * Synopsis:
    *
    * This method sets up the WebGL program.
    *
    * Attributions:
*
     * source: Skybox texture credit: Charles O'Rear
     *
     * author: Radovan Moncek
     */
    setupRendering() {
	function createShader(type, calls) {
	    const shader = this.gl.createShader(type)

	    this.gl.shaderSource(shader, calls)
	    this.gl.compileShader(shader)

	    if (!this.gl.getShaderParameter(shader, this.gl.COMPILE_STATUS)) {
		this.logger.logError(this.gl.getShaderInfoLog(shader))

		return null
	    }

	    return shader
	}

	createShader = createShader.bind(this)
	const program = this.gl.createProgram()

	this.gl.attachShader(program, createShader(this.gl.VERTEX_SHADER, Game.textureVertexShader))
	this.gl.attachShader(program, createShader(this.gl.FRAGMENT_SHADER, Game.textureFragmentShader))
	this.gl.linkProgram(program)

	if (!this.gl.getProgramParameter(program, this.gl.LINK_STATUS)) {
	    this.logger.logError(this.gl.getShaderInfoLog(program))

	    return null
	}

	this.gl.useProgram(program)

	this.locations.model = this.gl.getUniformLocation(program, "model")
	this.locations.view = this.gl.getUniformLocation(program, "view")
	this.locations.projection = this.gl.getUniformLocation(program, "projection")
	this.locations.sampler = this.gl.getUniformLocation(program, "sampler")
	this.locations.position = this.gl.getAttribLocation(program, "position")
	this.locations.textureCoord = this.gl.getAttribLocation(program, "textureCoord")
	this.transforms.projection = Game.identity
	this.transforms.view = Game.translate(0, this.cameraLow, this.cameraOffset)

	this.gl.enable(this.gl.DEPTH_TEST)
    }

    /**
    * Attributions:
*
     * source: Alternate skybox credit: wwwriks
     * source: Ground texture author: wwwriks, formerly Screaming Brain Studios
     *
     * author: Radovan Moncek
     */
    changeSceneGame() {
	//skybox? "https://raw.githubusercontent.com/wwwriks/wrad-textures/refs/heads/main/seamless/rust4.png"
	const ground = new Entity()

	ground.buffers=this.createGround()
	ground.buffers.texture = this.loadTexture("https://raw.githubusercontent.com/wwwriks/wrad-textures/refs/heads/main/seamless/concrete1.png")

	this.gl.pixelStorei(this.gl.UNPACK_FLIP_Y_WEBGL, true)
	this.entities.push(ground)

	ground.transforms.model = Game.multiplyMatrices(
	    Game.translate(0, -1, 0),
	    Game.scale(this.worldWidth, 1, this.worldWidth),
	    Game.isometricRotation
	)

	const announcmentContainer = document.createElement("div")
	
	announcmentContainer.style = "display: flex; flex-direction: column; align-items: center;"
	this.announcment = Game.createMessage("")

	announcmentContainer.append(this.announcment)
	this.uI.append(announcmentContainer)
    }

    changeSceneMenu() {
	//skybox? this.loadTexture("https://static.wikitide.net/windowswallpaperwiki/5/5a/Bliss-pic.jpg")
	const button = document.createElement("button")
	const sessionIdInput = document.createElement("input")
	const dummy = new Player()
	const menuOptions = document.createElement("div")
	const message1 = Game.createMessage("Create a new character")
	const message2 = Game.createMessage("Choose a name")
	const message3 = Game.createMessage("Choose a class")
	const message4 = Game.createMessage("Join a game")
	const nameInput = document.createElement("input")
	const blueOption = document.createElement("button")
	const greenOption = document.createElement("button")
	const menuOuter = document.createElement("div")

	let animationAction
	let characterDescription

	dummy.buffers=this.createPlayer()
	dummy.transforms.model = Game.multiplyMatrices(
	    Game.scale(1, 2, 1),
	    Game.translate(0,0,-2)
	)
	//dummy.buffers.texture = this.loadTexture("https://raw.githubusercontent.com/wwwriks/wrad-textures/refs/heads/main/seamless/concrete1.png")
	sessionIdInput.type="text"
	sessionIdInput.placeholder="Code"
	button.onclick = () => {
	    if (nameInput.value === "")
		return;

	    if (/*sessionIdInput.value === "" || */sessionIdInput.value !== "" && sessionIdInput.value.length !== 4) // magic
		return;

	    this.name = nameInput.value

	    this.webSocket.send(this.codec.multiplexEncode(sessionIdInput.value !== ""? Protocol.Reserved.joinExisting: Protocol.Reserved.joinNew, sessionIdInput.value))
	    this.uI.removeChild(menuOuter)

	    this.entities=this.entities.filter(entity=>entity!==dummy)
	}
	button.innerText="Join or create a game session"
	button.style="align-self:center;"
	menuOptions.style="display: flex; flex-direction: column;"
	menuOuter.style="display: flex; flex-direction: column; align-items: center;"
	nameInput.placeholder="Name"
	nameInput.style="align-self:center;"
	blueOption.innerText="Speed"
	blueOption.style="align-self:center;"
	blueOption.onclick=()=>{
	    //send request to pick this class
	    characterDescription.innerText="Moves faster"
	}
	greenOption.innerText="Strength"
	greenOption.style="align-self:center;"
	greenOption.onclick=()=>{
	    //send request to pick this class
	    characterDescription.innerText="Deals more damage"
	}
	characterDescription=document.createElement("center")
	characterDescription.style="position:fixed;bottom:40px;width:100%;"

	menuOptions.append(blueOption, greenOption)

	let rotationSpeed = 0.001

	animationAction = () => {
	    dummy.azimuth+=rotationSpeed*Math.PI

	    if (dummy.azimuth > 0.2||dummy.azimuth < -0.2)
		rotationSpeed*=-1

	    dummy.transforms.model = Game.multiplyMatrices(
		Game.rotateY(dummy.azimuth),
		dummy.transforms.model
	    )
	}

	dummy.animationActions.push(animationAction)

	menuOuter.append(message1, message2, nameInput, message3, menuOptions, message4, sessionIdInput, button, characterDescription)
	this.uI.append(menuOuter)
	this.entities.push(dummy)
    }

    setupCamera(fov, aspect, near, far) {
	const f = 1.0/Math.tan(fov/2)
	const inverseRange = Math.pow(near-far, -1)

	return [
	    f/aspect, 0, 0, 0,
	    0, f, 0, 0,
	    0, 0, (near + far) * inverseRange, -1,
	    0, 0, near * far * inverseRange * 2, 0
	]
    }

    /**
     * Attributions:
     *
     * source: THREE.js
     *
     * author: Radovan Moncek
     */
    startEventLoop() {
	let lastTime = 0

	const renderFrame = time => requestAnimationFrame(() => {
	    function position(entity) {
		this.gl.enableVertexAttribArray(this.locations.position)
		this.gl.bindBuffer(this.gl.ARRAY_BUFFER, entity.buffers.vertices)
		this.gl.vertexAttribPointer(this.locations.position, 3, this.gl.FLOAT, false, 0, 0)
	    }

	    function setColour(entity) {
		this.gl.enableVertexAttribArray(this.locations.color)
		this.gl.bindBuffer(this.gl.ARRAY_BUFFER, entity.buffers.colours)
		this.gl.vertexAttribPointer(this.locations.color, 4, this.gl.FLOAT, false, 0, 0)
	    }
	    
	    function setTexture(entity) {//think of a better name that does not sound like a setter
		if (!entity.buffers.texture)
		    return

		this.gl.enableVertexAttribArray(this.locations.textureCoord)
		this.gl.bindBuffer(this.gl.ARRAY_BUFFER, entity.buffers.colours)
		this.gl.vertexAttribPointer(this.locations.textureCoord, 2, this.gl.FLOAT, false, 0, 0)
	    }

	    const deltaTime = time-lastTime
	    const step = this.speed * deltaTime
	    const player = this.entities.filter(entity=>entity.uniqueId===this.assignedUniqueId)[0]
	    const stateRequest = new Player()

	    let keyPressed = false

	    position = position.bind(this)
	    setColour = setColour.bind(this)
	    setTexture = setTexture.bind(this)
	    this.fpsStatistic.innerText=`${Math.ceil(deltaTime)}fps`
	    lastTime = time
	    this.transforms.projection = this.setupCamera(Math.PI*0.5, window.innerWidth/window.innerHeight, 1, 50)

	    for (const entity of this.entities) {
		entity.performAnimationActions()
		position(entity)
		setTexture(entity)
		this.gl.bindBuffer(this.gl.ELEMENT_ARRAY_BUFFER, entity.buffers.indices)
		this.gl.uniformMatrix4fv(this.locations.model, false, new Float32Array(entity.transforms.model.flat()))
		this.gl.uniformMatrix4fv(this.locations.projection, false, new Float32Array(this.transforms.projection.flat()))
		this.gl.uniformMatrix4fv(this.locations.view, false, new Float32Array(this.transforms.view.flat()))
		this.gl.activeTexture(this.gl.TEXTURE0)
		this.gl.bindTexture(this.gl.TEXTURE_2D, entity.buffers.texture)
		this.gl.uniform1i(this.locations.sampler, 0)
		this.gl.drawElements(this.gl.TRIANGLES, entity.buffers.triangleCount, this.gl.UNSIGNED_SHORT, 0)
	    }

	    if (this.assignedUniqueId&&player) {
		stateRequest.position = {x:player.position.x, y:player.position.y, z:player.position.z}
		stateRequest.azimuth = player.azimuth

		if(this.heldKeys.has("KeyW")){
		    stateRequest.position.x+=step*Math.sin(player.azimuth)
		    stateRequest.position.z+=step*-Math.cos(player.azimuth)
		    keyPressed=true
		}

		if(this.heldKeys.has("KeyS")){
		    stateRequest.position.x-=step*Math.sin(player.azimuth)
		    stateRequest.position.z-=step*-Math.cos(player.azimuth)
		    keyPressed=true
		}

		if(this.heldKeys.has("KeyA")){
		    stateRequest.position.x+=step*Math.sin(player.azimuth-Math.PI/4)
		    stateRequest.position.z-=step*Math.cos(player.azimuth-Math.PI/4)
		    keyPressed=true
		}

		if(this.heldKeys.has("KeyD")){
		    stateRequest.position.x-=step*Math.sin(player.azimuth-Math.PI/4)
		    stateRequest.position.z+=step*Math.cos(player.azimuth-Math.PI/4)
		    keyPressed=true
		}

		if(this.heldKeys.has('Space')){
		    stateRequest.position.y=2
		    keyPressed=true
		}

		if (this.movementX!==0){
		    stateRequest.azimuth-=this.movementX*this.mouseSensitivity*Math.PI
		}

		if (this.firing) {
		    stateRequest.firing=0x1
		}

		if (this.firing||this.movementX!==0||(this.heldKeys.size !== 0 && keyPressed)) {
		    this.firing=false
		    this.movementX=0
		    stateRequest.uniqueId=player.uniqueId
		    stateRequest.name=this.name??"Emptiness"

		    if (this.webSocket.readyState===WebSocket.OPEN)
			this.webSocket.send(this.codec.multiplexEncode(Protocol.Usable.player, stateRequest))
		}
	    }

	    (async () => {
		await Game.sleep(Math.max(this.serverTickRate, this.serverTickRate-deltaTime))

		renderFrame(Date.now())
	    })()
	})

	renderFrame(Date.now())
    }
}

new Game(["192.168.100.81"], 4321)

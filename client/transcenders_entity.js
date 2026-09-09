export class Entity {
    uniqueId=null
    azimuth=0
    animationActions=[]
    drawCalls = []
    position = {x:0,y:0,z:0}
    buffers
    transforms = {model:null}

    performAnimationActions() {
	for (let animation of this.animationActions)
	    animation()
    }
}

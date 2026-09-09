import { Entity } from './transcenders_entity.js'

export class Player extends Entity {
    name=""
    classification=null
    firing=0x0

    changeClassification(newClassification) {//todo
	this.classification=newClassification
    }
}

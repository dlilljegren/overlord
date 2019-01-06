import * as render from "./renderer.mjs" 
import * as canvas from "./canvas.mjs"

const ctx = canvas.CANVAS_MAP.get("zoc").getContext("2d");


export function onZoc(zocMessage) {
    if(zocMessage.isSnapshot){
        render.clearRect(zocMessage.sectionRect);
    }
    //canvas.clear("zoc");    
    for (let [team, cords] of Object.entries(zocMessage.gained)) {
        const name = `Zoc${team}`;
        cords.forEach ( c=>render.draw(name,ctx,c.col,c.row))
    }
    for (let [team, cords] of Object.entries(zocMessage.lost)) {        
        cords.forEach ( c=>render.clear(ctx,c))
    }
}
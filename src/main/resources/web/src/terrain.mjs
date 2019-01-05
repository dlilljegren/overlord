import * as constants from "./constants.mjs" 
import * as render from "./renderer.mjs" 
import * as canvas from "./canvas.mjs"

const terrainCtx = canvas.CANVAS_MAP.get("terrain").getContext("2d");
const baseCtx = canvas.CANVAS_MAP.get("base").getContext("2d");

const hashFunc = constants.hashFunc;
const hashFunc2 = constants.hashFunc2;
const occupied = new Set();


//see https://developers.google.com/web/updates/2018/08/offscreen-canvas#additional_resources
export function onSnapshot(snapshotMsg){          
    occupied.clear();
    //Find terrain array
    const terrainArray = snapshotMsg.terrains;
    for(const e of terrainArray){
        const name = e.e;
        const cord  = e.at;
        occupied.add(hashFunc(e.at))
        render.draw(name,terrainCtx,cord.col,cord.row);

    }
    //Find base array
    const baseArray = snapshotMsg.bases;
    for(const b of baseArray ){
        const base = b.e;
        for(const ba of base.area){
            occupied.add(hashFunc(ba))
            render.draw("Base2",baseCtx,ba.col,ba.row);
            //render.draw("Base2Black",baseCtx,ba.col+5,ba.row);

        }
        for(const zoc of base.zoc){
            const col = zoc.col;
            const row = zoc.row;
            render.draw("ZocBase",baseCtx,col,row);
        }
    }
    
    render.drawTest(baseCtx);
}

export function isOccupied(cord){
    return occupied.has(hashFunc(cord));
}

export function isFree(col,row){
    return !occupied.has(hashFunc2(col,row));
}

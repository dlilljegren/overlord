import * as info from "./serverInfo.mjs";
import * as constants from "./constants.mjs";
import * as toServer from "./toServer.mjs";
import * as canvas from "./canvas.mjs";
import * as render from "./renderer.mjs";
import * as terrain from "./terrain.mjs";
import * as teamInfo from "./teamInfo.mjs";

const S = constants.SQR_SIZE;

const message = info.setInfoMessage;
const hashFunc = constants.hashFunc2;
const hashFuncCord = constants.hashFunc;
const selectionCanvas = canvas.CANVAS_MAP.get('selection');
const unitCanvas = canvas.CANVAS_MAP.get('unit');
const ctx = unitCanvas.getContext('2d');

const units = new Map();

function hasUnit(c,r){
    return units.has(hashFunc(c,r)) 
}
function getTeam(c,r){
    return units.get(hashFunc(c,r)).team;
}

export function onSnapshot(snapshot){
    for(const b of snapshot.units){                
        addUnit(b.at,b.e);
    }
}

export function onAddUnit(addUnitMsg){
    const c = addUnitMsg.cord;
    const u = addUnitMsg.unit;
    addUnit(c,u);
}
export function onRemoveUnit(removeUnitMsg){
    removeUnit(removeUnitMsg.cord);
}

function addUnit(cord,unit){
    units.set(hashFuncCord(cord), unit)
    drawUnit(cord,unit.team);
}

function removeUnit(cord){
    units.delete(hashFuncCord(cord));
    render.clearUnitWithTrace(ctx,cord);
    setTimeout( ()=>clearIfEmpty(cord) , 2000);
}

function drawUnit(cord,team){
    const name = `Unit${team}`;
    render.draw(name,ctx,cord.col,cord.row);
}

function clearIfEmpty(cord){
    if(!hasUnit(cord.col,cord.row)){
        render.clear(ctx,cord);
    }
}

let x,y,col,row;
function addListener(){
    const target = selectionCanvas;
    const newListener = e=>{
        const x = e.offsetX;
        const y = e.offsetY;
        col = Math.floor(x / S);
        row = Math.floor(y / S);
        
        if(!terrain.isFree(col,row)){
            message("Can't place here");
            return;
        } 
        if(hasUnit(col,row)) {
            if(teamInfo.isMyTeam(getTeam(col,row))){
                toServer.sendRemove(col,row);
            }
            else{
                message("Can't remove unit of other team");
                return;
            }
        }
        else{
            toServer.sendAdd(col,row);
        }
    };
    target.addEventListener("click",newListener,false);
}

addListener();
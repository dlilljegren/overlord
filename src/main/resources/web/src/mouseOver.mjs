import * as constants from "./constants.mjs"
import * as canvas from "./canvas.mjs";
import * as render from "./renderer.mjs";
import * as terrain from "./terrain.mjs";

const selectionCanvas = canvas.CANVAS_MAP.get('selection');
const viewCord = document.getElementById('current-view-cord');
const worldCord = document.getElementById('current-world-cord');
const ctx = selectionCanvas.getContext('2d');

const SQR_SIZE = constants.SQR_SIZE;

let currentListener;



export function setupMouseOver(initViewMsg ){
   
   
    const offsetCord = initViewMsg.viewDefinition.origo;
    const xOffset = offsetCord.col;
    const yOffset = offsetCord.row;
    const target = selectionCanvas;
    if(currentListener){
        target.removeEventListener("mousemove",currentListener,true);
    }
  
    const moImage = imageCreatorFunc(190,0,210,100);
    const clearImage = imageCreatorFunc(0,0,0,0);
    const isFree = terrain.isFree;

    let col,row;
    const newListener = e=>{
        //Delete the last
        ctx.putImageData(clearImage, col*SQR_SIZE, row*SQR_SIZE);
        const x = e.offsetX;
        const y = e.offsetY;
        col = Math.floor(x / SQR_SIZE);
        row = Math.floor(y / SQR_SIZE);
        const colServerWorld= col +xOffset;
        const rowServerWorld = row +yOffset;
        viewCord.textContent = `${pad(col)} x ${pad(row)}`;
        worldCord.textContent = `${pad3(colServerWorld)} x ${pad3(rowServerWorld)}`;

        if(isFree(col,row)){
            ctx.putImageData(moImage,col*SQR_SIZE, row*SQR_SIZE);
        }
        else{
            render.draw("No",ctx,col,row);
        }
        
    };
    target.addEventListener("mousemove",newListener,true);
    currentListener = newListener;
}

function imageCreatorFunc(r,g,b,a){
    const imageData = ctx.createImageData(SQR_SIZE, SQR_SIZE);
    // Iterate through every pixel
    for (let i = 0; i < imageData.data.length; i += 4) {
        // Modify pixel data
        imageData.data[i + 0] = r;  // R value
        imageData.data[i + 1] = g;  // G value
        imageData.data[i + 2] = b;  // B value
        imageData.data[i + 3] = a;  // A value
    }
    return imageData;
}

function pad(x){
    return x.toString().padStart(2,'0');
}
function pad3(x){
    return x.toString().padStart(3,'0');
}
import * as constants from "./constants.mjs" 
import * as render from "./renderer.mjs" 
import * as sectionInfo from "./sectionInfo.mjs" 
import * as canvas from "./canvas.mjs"

const canvasWrapper = document.getElementById('canvas-wrapper');



const viewDimension = document.getElementById('view_dimension');
const sectionVersion = document.getElementById('section-version');

const sectionCtx = sectionCanvas.getContext('2d');

const SQR_SIZE = constants.SQR_SIZE;



//Draw the background
var ctx = bgCanvas.getContext('2d',{ alpha: false });
function setCanvasSize(c, w, h) {
    c.width = w;
    c.height = h;
}
function drawLine(ctx, start, end){
    ctx.beginPath();
    ctx.moveTo(start.col * SQR_SIZE, start.row * SQR_SIZE);
    ctx.lineTo(end.col * SQR_SIZE, end.row * SQR_SIZE);
    ctx.stroke();
}

export async function onInitView(initViewMsg){
    const vd = initViewMsg.viewDefinition;
    //viewDef.innerText = `[${vd.origo.col}->${vd.width} :  ${vd.origo.row}->${vd.width}]`;
    
    //Set mainSection
    sectionInfo.setSection(vd.mainSection);
    const si = initViewMsg.sectionVersion;
    sectionVersion.textContent = `${si}`;
    const widthRequired = vd.width * SQR_SIZE;
    const heightRequired = vd.height * SQR_SIZE;
    //Configure the canvas
    //See https://stackoverflow.com/questions/331052/how-to-resize-html-canvas-element
    //canvasWrapper.width = widthRequired;
    //canvasWrapper.height = heightRequired;
    canvas.CANVAS_MAP.forEach(
        (canvas) => setCanvasSize(canvas, widthRequired, heightRequired)
    );
    render.drawBackground(ctx);
    
  
    //Draw the view borders            
    const map = new Map(Object.entries(initViewMsg.sectionBorders));//Can't just take the raw JSON objects
    for (let [section, borders] of map.entries()) {
        const bordersMap = new Map(Object.entries(borders.borders))
        for (let [direction, line] of bordersMap.entries()) {
            sectionCtx.strokeStyle = "#FF0000";
            sectionCtx.setLineDash([15, 2]);
            if (direction == 'North') {
                line.end.col++;
            }
            else if (direction == 'West') {
                line.end.row++;
            }
            else if (direction == 'South') {
                line.start.row++;
                line.end.row++;
            }
            else if (direction == 'East') {
                line.start.col++;
                line.end.col++;
                line.end.row++;
            }
            drawLine(sectionCtx, line.start, line.end);
        }
    }
}
    
import * as constants from "./constants.mjs" 
import TileSet from "./tileSet.mjs"
const S = constants.SQR_SIZE;



//Draw one icon per row, with the shaded versions for each team on column 1,2,3 
//Or for terrain the different orientations 
const offScreen = new OffscreenCanvas(9*S, 10*S);

const ctx = offScreen.getContext('2d');

let offsets = new Map();


async function drawAll2(){
    //Start loading images async
    const lakeTile = new TileSet("lake.png",100)
    const shields =  new TileSet("tiles1.png",77,0,56)
    const misc    =  new TileSet("tiles2.png",32)
    const mount   =  new TileSet("mountains.png",64)

    dl(lakeTile,"WaterNW",0,0,0,0);
    dl(lakeTile,"WaterNN",1,0,1,0);
    dl(lakeTile,"WaterNE",2,0,2,0);
    dl(lakeTile,"WaterWW",0,1,3,0);
    dl(lakeTile,"Water"  ,1,1,4,0);
    dl(lakeTile,"WaterEE",2,1,5,0);
    dl(lakeTile,"WaterSW",0,2,6,0);
    dl(lakeTile,"WaterSS",1,2,7,0);
    dl(lakeTile,"WaterSE",2,2,7,0);

    await dlAllTeams(shields, "Unit",0,0,0,1)
    offsets.set("Eagle1",offsets.get("Unit"));
    
    dl(shields, "Eagle2",1,0,0,2)

    dl(shields, "Eagle3",2,0,0,3)

    dl(shields, "Eagle4",3,0,0,4)
    

    dl(misc,"Base2"     ,5,9,0,5);
    dl(misc,"Base2Red"  ,5,9,1,5,"Red");
    dl(misc,"Base2Blue" ,5,9,2,5,"Blue");
    dl(misc,"Base2Black",5,9,3,5,"Black");

    dl(mount,"Hill",2,4,0,6,null,2)
    
    dl(mount,"Mountain",3,4,1,6)
    
    ctx.save();
    zocForTeam("ZocRed",0,7,"Red")
    zocForTeam("ZocBlue",1,7,"Blue")
    zocForTeam("ZocBlack",2,7,"Black")
    zocForBase("ZocBase",3,7)
    ctx.restore();
}

function dlAllTeams(ts,name,tileX,tileY,destX,destY,adjust){
    const teams = constants.TEAMS;
    const noTeam = dl(ts,name,tileX,tileY,destX,destY,adjust); 
    const promises = teams.map( (t,i)=>dl(ts,`${name}${t}`,tileX,tileY,destX+i+1,destY,t,adjust));
    return Promise.all(promises);
}

async function dl(ts,name,tileX,tileY,destX,destY,team,adjust){
    offsets.set(name, await ts.draw(tileX,tileY,1,1, ctx, destX,destY,adjust));

    if(team){
        ctx.save();
        ctx.globalCompositeOperation="source-atop";
        ctx.globalAlpha=0.3;
        ctx.fillStyle = constants.teamColor(team);
        ctx.fillRect(destX*S, destY*S, S, S);
        ctx.restore();
    }
}

function zocForTeam(name,destX,destY,team){
    return zoc(name,destX,destY,constants.teamColor(team))
}
function zocForBase(name,destX,destY){
    return zoc(name,destX,destY,"rgb(25,25,25)")
}

async function zoc(name,destX,destY,color){
    offsets.set(name, {x:destX*S,y:destY*S,w:S,h:S})    
    ctx.fillStyle= color;    
    ctx.globalAlpha=0.2;
    ctx.fillRect(destX*S, destY*S, S, S);
}

function toRgba(rgb,alpa){
    return `rgba(${rgb.r},${rgb.g},${rgb.b},${alpha})`;
}

async function drawAll(){

    //const lake = loadImageAsync("lake.png");
    //const hill = loadImageAsync("hill1.png");
    const baseP = loadImageAsync("black-base.png");
    const noP = loadImageAsync("cancel.png");
    
    /*
    const redP = loadImageAsync("piecesRed.png");
    const blueP = loadImageAsync("piecesBlue.png");
    const blackP = loadImageAsync("piecesBlack.png");
    */

    //ctx.drawImage(await lake,0,0,300,300,0,0,S*3,S*3);
    //ctx.drawImage(await hill,0,0,25,25,S*3,S*0,S,S);
    
    const base = await baseP;
    ctx.drawImage(base,0,0,base.width,base.height,S*3+2,S*1+2,S-4,S-4);
    
    //let w = 32;
    //let h = 32;
    //ctx.drawImage(ts1,32*5,32*9,w,h,S*4+2,S*1+2,20,20);

    const no = await noP;
    ctx.drawImage(no,0,0,no.width,no.height,S*3+2,S*2+2,S-4,S-4);

    //const zocImage = await imageCreatorFunc(20,20,20,100);
    //ctx.putImageData(zocImage,S*4,S*0);

    const clearImage = await imageCreatorFunc(0,0,0,0);
    ctx.putImageData(clearImage,S*3,S*3);

    //drawPiece("Red", await redP,0,11,0)
    //drawPiece("Blue", await blueP,1,11,0)
    //drawPiece("Black", await blackP,2,11,0)

    //drawZoc("Red",0);
    //drawZoc("Blue",1);
    //drawZoc("Black",2);

    
    console.info("All images drawn to offscreen");
}



function drawPiece(name,bigImg,no,sc,sr){
    const sx = sc * 64;
    const sy = sr * 64;
    ctx.drawImage(bigImg,sx,sy,64,64,S*(5+no), S*0 , S,S);
    offsets.set(`Unit${name}`, os(5+no,0));    
}

function drawZoc(team,no){
    ctx.save();
    ctx.fillStyle = constants.teamColor(team);
    ctx.globalAlpha = 0.2;
    ctx.fillRect(S*(5+no), S*1, S, S);
    offsets.set(`Zoc${team}`, os(5+no,1));    
    ctx.restore();
}

/*
offsets.set("WaterNW", {x:0*S,y:0*S,w:S,h:S});
offsets.set("WaterNN", {x:1*S,y:0*S,w:S,h:S});
offsets.set("WaterNE", {x:2*S,y:0*S,w:S,h:S});
offsets.set("WaterWW", {x:0*S,y:1*S,w:S,h:S});
offsets.set("Water"  , {x:1*S,y:1*S,w:S,h:S});
offsets.set("WaterEE", {x:2*S,y:1*S,w:S,h:S});
offsets.set("WaterSW", {x:0*S,y:2*S,w:S,h:S});
offsets.set("WaterSS", {x:1*S,y:2*S,w:S,h:S});
offsets.set("WaterSE", {x:2*S,y:2*S,w:S,h:S});
*/


offsets = new Map(  [...offsets].map(([k, v]) => [k, {x:v.x+1, y:v.y+1, w:v.w-2, h:v.h-2}]) );

//offsets.set("Hill", {x:3*S,y:0*S,w:S,h:S});

offsets.set("Base",    os(3,1));


offsets.set("No" ,     os(3,2));
offsets.set("Clear" ,  os(3,3));
//offsets.set("ZocBase", os(4,0));


function os(c,r){
    return {x:c*S,y:r*S,w:S,h:S};
}

let tmp;
/**
 * Draw the resource of the given name at the given column and row
 * @param name 
 * @param destCtx 
 * @param x 
 * @param y 
 */
export function draw(name,destCtx,col,row){
    tmp = offsets.get(name);
    if(!tmp) console.error(`Don't know how to render:${name}`);
    destCtx.drawImage(offScreen,tmp.x,tmp.y,tmp.w,tmp.h,col*S,row*S,tmp.w,tmp.h);
}

export function clearUnitWithTrace(destCtx,cord){
    const tmp = destCtx.globalCompositeOperation;
    destCtx.globalCompositeOperation = "xor";
    draw("UnitRed",destCtx,cord.col,cord.row);
    destCtx.globalCompositeOperation = tmp
}
export function clear(destCtx,cord){
    destCtx.clearRect(cord.col*S,cord.row*S,S,S);
}

export function clearRect(destCtx,rect){
    destCtx.clearRect(rect.col*S,rect.row*S,rect.width*S,rect.height*S);
}

export function redBase(destCtx,col,row){
    draw("Base2",destCtx,col,row);
    destCtx.globalCompositeOperation="source-atop";
    //destCtx.fillStyle = 'red';
    destCtx.fillStyle = constants.RED_TEAM_COLOR;
    destCtx.fillRect(col*S, row*S, S, S);
    destCtx.globalCompositeOperation ="source-over";
}
export function drawWithTeamColor(name,team,destCtx,col,row){
    draw(name,destCtx,col,row);
    destCtx.save();
    destCtx.globalCompositeOperation="source-atop";
    destCtx.globalAlpha = 0.3;
    destCtx.fillStyle = constants.teamColor(team);
    destCtx.fillRect(col*S, row*S, S, S);
    destCtx.restore();
}

export async function drawBackground(destCtx){
    const BG_IMAGE = await loadImageAsync('squares64.png');
    const pattern = destCtx.createPattern(await BG_IMAGE, 'repeat');
    destCtx.fillStyle = pattern;
    destCtx.fillRect(0, 0, destCtx.canvas.width, destCtx.canvas.height);
}

export async function loadImageAsync(file) {
    const url = 'img/' + file;
    return new Promise((resolve, reject) => {
        let img = new Image();
        img.addEventListener('load', e => resolve(img));
        img.addEventListener('error', () => {
            reject(new Error(`Failed to load image's URL: ${url}`));
        });
        img.src = url;
    });
}

async function imageCreatorFunc(r,g,b,a){
    const imageData = ctx.createImageData(S, S);
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

export const HILL = loadImageAsync('hill1.png');
export const WATER = loadImageAsync('wave.png');

export const RED = loadImageAsync('piecesRed.png');
export const BLUE = loadImageAsync('piecesBlue.png');
export const BLACK = loadImageAsync('piecesBlack.png');

export const BLACK_BASE = loadImageAsync('black-base.png');

export async function drawTest(ctx){
   

    /*
    var ts = new TileSet("shields.jpg",76,0,56);
    ts.draw(0,0,1,1,ctx,9,9);

    ts.draw(1,0,1,1,ctx,11,9);

    ts.draw(2,0,1,1,ctx,13,9);
    */

    draw("Eagle1",ctx,9,9);
    draw("Eagle2",ctx,10,9);
    draw("Eagle3",ctx,11,9);
    draw("Eagle4",ctx,12,9);
}

drawAll();
drawAll2();

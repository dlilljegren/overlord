import * as render from "./renderer.mjs"
import * as constants from "./constants.mjs"

const S = constants.SQR_SIZE;
export default class TileSet{
     

    constructor(fileName,size,offsetX,offsetY){
        this.img = render.loadImageAsync(fileName);
        this.size= size;
        this.offsetX = offsetX ? offsetX : 0;
        this.offsetY = offsetY ? offsetY : 0;
    }

    /**
     * Draw on e.g. offscreen canvas 
     * @param colTile the x column in the tile set
     * @param rowTile the y row in the tile set
     * @param wFactor the width in the tile set as times tile size 
     * @param hFactor the height in the tile set as times tile size 
     * @param destCtx the offscren canvas to draw on
     * @param colDest the destination column at the offscreen canvas 
     * @param rowDest the row column at the offscreen canvas
     */
    async draw(colTile,rowTile,wFactor,hFactor,destCtx,colDest,rowDest,adjust){
        if(!adjust) adjust =0;
        const sx = colTile*this.size+this.offsetX;
        const sy = rowTile*this.size+this.offsetY;
        const sw = wFactor*this.size;
        const sh = hFactor*this.size;
        const dx = colDest * S + adjust;
        const dy = rowDest * S + adjust;

        const dh = S - 2*adjust
        const dw = Math.floor(S*wFactor/hFactor) -2*adjust;
        destCtx.drawImage(await this.img,sx,sy,sw,sh,dx,dy,dw,dh)

        return {x:colDest*S,y:rowDest*S,w:S,h:S}
    }
}
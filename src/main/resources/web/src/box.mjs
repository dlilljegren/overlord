export function hello() {
    console.info("Hi Module")
}

export function clickUp(){
    console.info("clickUp");
    draw();
    spriteNo++;
}
export function clickDown(){
    console.info("clickDown");
    draw();
    spriteNo--;
}

let spriteNo=0;
let ctx;
export function draw(){
    const x = 0;
    const y = 0;

    ctx.moveTo(x, y);
    ctx.fillStyle = "#FFF";
    ctx.fillRect(x,y,65,80);

    ctx.rect(5,0,65,80);
    ctx.rect(85,0,65,80);
    ctx.rect(165,0,65,80);
    
    ctx.rect(5,85,65,80);
    //ctx.lineTo(200, 100);
    ctx.stroke();
    
    const sx = 5 + 80* Math.floor(spriteNo%7);
    const sy = 0 + 85* Math.floor(spriteNo/7);
    const swidth=65;
    const sheight=80;
    //ctx.drawImage(ROBOT,)
    ctx.drawImage(ROBOT,sx,sy, swidth, sheight, x, y, swidth, sheight );
    
}

export function setContext(c){
    ctx = c;
}

function loadImage(url){
    var image=new Image();
    image.src = 'img/'+url;
    image.onload = x => console.log("Loaded:"+url);
    return image;
}

const ROBOT = loadImage("robot.png");

console.info("module box loaded");
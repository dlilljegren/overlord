





function loadImage(url){
    var image=new Image();
    image.src = 'img/'+url;

    image.onload = x => console.log("Loaded:"+url);

    return image;
}

function loadImageData(url, setter){
    var image=new Image();
    image.src = 'img/'+url;

    image.onload = x=>{
        const canvas = document.createElement("CANVAS");
        canvas.width  = image.width;
        canvas.height = image.height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(image,0,0,image.width,image.height);

        setter(ctx.getImageData(0, 0, image.width, image.height) );
    }
}

 const HILL = loadImage('hill1.png');
 const WATER = loadImage('wave.png');

 const RED = loadImage('piecesRed.png');
 const BLUE = loadImage('piecesBlue.png');
 const BLACK = loadImage('piecesBlack.png');

 const BLACK_BASE = loadImage('black-base.png');

 var NO;
 loadImageData('cancel.png', x=>NO=x);
const canvases =[
    "bg",
    "terrain",
    "selection",
    "section",
    "base",
    "unit",
    "zoc"
];

export const CANVAS_MAP = new Map(canvases.map(n => [n, document.getElementById(`${n}Canvas`)]));

export function clear(name){
    clearCanvas(CANVAS_MAP.get(name));
}

function clearCanvas(canvas){
    canvas.width = canvas.width;
}
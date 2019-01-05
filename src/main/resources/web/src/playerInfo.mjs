const names = [
    "units",
    "casualties",
    "kills"
];


const player = document.getElementById("player-name");
const units  = document.getElementById("player-units")
const causalties  = document.getElementById("player-causalties")

const map = new Map();
for(let n of names){
    map.set(n, document.getElementById(`player-${n}`));
}


export function setPlayer(playerName){
    set(player,playerName)
    for(let [k,v] of map){
        set(v,'0');
    }
}

export function setUnitStatus(unitStatus){
    set(units,unitStatus.units)
}

function set(element,text){
    element.textContent = text;
}


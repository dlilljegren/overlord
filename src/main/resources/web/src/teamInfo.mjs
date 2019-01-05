const names = [
    "units",
    "casualties",
    "kills",
    "players",
    "sectors",
    "bases",
]

const team = document.getElementById("team-name");
let myTeam;

const map = new Map();
for(let n of names){
    map.set(n, document.getElementById(`team-${n}`));
}


export function setTeam(teamName){
    team.innerText = `Team ${teamName}`;
    myTeam = teamName;
    for(let [k,v] of map){
        set(v,'\u00A0');
    }
}

export function isMyTeam(teamName){
    return myTeam == teamName;
}

function set(element,text){
    element.textContent = text;
}
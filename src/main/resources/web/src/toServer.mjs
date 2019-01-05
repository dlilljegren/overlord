

let webWorker;

export function setWorker(worker){
    webWorker = worker;
}


export function registerPlayerWithTeam(player,teamName){
    const obj = {};
    obj.player = player;
    obj.team = teamName;
    obj.cmd  = "registerPlayer";
    obj.p='p';
    
    webWorker.postMessage(obj);
}

export function login(cookie){
    const obj = {};
    obj.userCookie = cookie;
    obj.cmd = "login"
    obj.p='l';
    
    webWorker.postMessage(obj);
}

export function sendAdd(col,row){
    const obj = {};
    obj.cord = {};
    obj.cord.col = col;
    obj.cord.row = row;
    obj.cmd ="add";
    obj.p = "a";
    
    webWorker.postMessage(obj);
}
export function sendRemove(col,row){
    const obj = {};
    obj.cord = {};
    obj.cord.col = col;
    obj.cord.row = row;
    obj.cmd ="remove";
    obj.p = "r";
    
    webWorker.postMessage(obj);
}

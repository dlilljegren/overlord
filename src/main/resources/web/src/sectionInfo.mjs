const title = document.getElementById("section-title");

const worldSize = document.getElementById("world-size");
const sectionSize = document.getElementById("section-size");


export function setSection(sectionNo){
    const no =sectionNo.toString().padStart(2,'0');
    title.textContent =`SECTION ${no}`; 
}

export function setInitGame(initGameMsg){
    setSize(worldSize,  initGameMsg.worldWidth,initGameMsg.worldHeight);
    setSize(sectionSize,initGameMsg.sectionWidth,initGameMsg.sectionHeight);
}

function setSize(target,w,h){
    target.textContent = `${pad(w)} x ${pad(h)}`;
}

function pad(x){
    return x.toString().padStart(2,'0');
}


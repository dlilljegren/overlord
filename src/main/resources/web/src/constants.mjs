export const SQR_SIZE = 32;
export const TEAMS =["Red","Blue","Black"];

//get from css file
export const RED_TEAM_COLOR = getComputedStyle(document.querySelector(".red-team")).backgroundColor
export const BLUE_TEAM_COLOR = getComputedStyle(document.querySelector(".blue-team")).backgroundColor
export const BLACK_TEAM_COLOR = getComputedStyle(document.querySelector(".black-team")).backgroundColor

const team2Color = new Map();
findTeamColor("Red");
findTeamColor("Blue");
findTeamColor("Black");

function findTeamColor(team){
    const c = getComputedStyle(document.querySelector(`.${team.toLowerCase()}-team`)).backgroundColor;
    team2Color.set(team,c);
}

export function hashFunc(cord){
    return cord.col*10000+cord.row;
}

export function hashFunc2(col,row){
    return col*10000+row;
}

export function teamColor(team){
    return team2Color.get(team);
}

import * as toServer from "./toServer.mjs"

let player;


const selectTeamScreen = document.getElementById("selectTeamScreen");
const registerPlayerScreen = document.getElementById("registerPlayerScreen");
const buttons = document.getElementsByClassName("svg-wrapper");



for(var i=0;i<buttons.length;i++){
    const button = buttons[i];

    button.addEventListener("click",()=>{       
        const team = button.attributes.team.value;
        teamSelected(team);
    });
}

export function onServerConnect(){
    //check the local storage for cookie
    const cookie=window.localStorage.getItem("cookie");

    if(cookie){
        //We have an id we should proceed to direct login
        toServer.login(cookie);
    }
    else{
        //We should register a new player
        show(registerPlayerScreen);
    }

}

export function onPlayerName(playerName){
    player = playerName;
    //Hide register player
    hide(registerPlayerScreen);
    //Show the select team screen
    show(selectTeamScreen);
}

export function onLogin(playerName,team,userCookie){
    window.localStorage.setItem("cookie",userCookie);
}


function teamSelected(teamName){
    console.info(`Selected team ${teamName}`);
    hide(selectTeamScreen);

    toServer.registerPlayerWithTeam(player,teamName)
}



function hide(screen){
    screen.style.display="none";
}
function show(screen){
    screen.style.display="flex";
}
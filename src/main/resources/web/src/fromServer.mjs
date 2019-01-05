import * as screen from "./screen.mjs" 
import * as playerInfo from "./playerInfo.mjs"
import * as teamInfo from "./teamInfo.mjs"
import * as sectionInfo from "./sectionInfo.mjs"
import * as serverInfo from "./serverInfo.mjs"
import * as view from "./viewInfo.mjs"
import * as mouseOver from "./mouseOver.mjs"
import * as terrain from "./terrain.mjs"
import * as units from "./addUnit.mjs"
import * as zoc from "./zoc.mjs"
/**
 * Class that process all incoming messages from server
 */
export default class FromServer{
    constructor(webWorker){
        this.webWorker = webWorker;

        let cmd,payload;

        this._assignPlayer = makeAssignPlayer(); 
        
        this.webWorker.onmessage = t=>{
            cmd = t.data[0];
            payload = t.data[1];

            if(cmd =="AddUnit"){
                units.onAddUnit(payload);
            }
            else if(cmd =="RemoveUnit"){
                units.onRemoveUnit(payload);
            }
            else if(cmd=="ZoneOfControl"){
                zoc.onZoc(payload);
            }
            else if(cmd=="AssignPlayer"){
                this._assignPlayer(payload);
            }
            else if(cmd=="InfoMsg"){
                serverInfo.setInfoMessage(payload.message);
            }
            else if(cmd =="InitGame"){
                sectionInfo.setInitGame(payload);
            }
            else if(cmd =="InitView"){
                view.onInitView(payload);
                mouseOver.setupMouseOver(payload );
            }
            else if(cmd == "Snapshot"){
                terrain.onSnapshot(payload);
                units.onSnapshot(payload);
            }
            else if(cmd =="connected"){
                serverInfo.setInfoMessage("WebSocket connected");
                screen.onServerConnect();
            }
            else if(cmd=="AssignSession"){
                serverInfo.setInfoMessage("Session assigned ok");
            }
            else if(cmd =="UnitStatus"){
                playerInfo.setUnitStatus(payload);
            }

            else{
                console.error("Unknown cmd:"+cmd);
            }
        }
    }

    
}



function makeAssignPlayer(){


    return msg=>{
        const player = msg.player.name;
        const team   = msg.player.team;
        const cookie = msg.userCookie;
        screen.onLogin(player,team,cookie);
        playerInfo.setPlayer(player);
        teamInfo.setTeam(team);

        sectionInfo.setSection(2);
    }
}
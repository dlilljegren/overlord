
let session;

/**
 * From main thread to worker thread
 */
onmessage = function (e) {
	console.log("Game script sent:" + JSON.stringify(e.data));
	if (e.data.cmd == "connect") {
		console.info("Connect called");
		sendInitGame();
	}
	else if (e.data.cmd == "close") {
		if (session) session.close();
		this.close();
	}
	else if (e.data.cmd == "registerPlayer") {
		const playerCreated = {};
		playerCreated.player = e.data.player;
		playerCreated.team = e.data.team;
		playerCreated.userCookie = "123ABC";
		postMessage(["AssignPlayer", playerCreated]);
	}
	else if (e.data.cmd == "login") {
		const player = {};
		player.player = "Hubba";
		player.team = "Red";
		player.userCookie = "123ABC";
		postMessage(["AssignPlayer", player]);

		sendTwo();
		sendViewInit();
		sendSnapshot();
	}
	else if (e.data.cmd == "add") {
		sendAddUnit("Red", e.data.cord.col, e.data.cord.row)
		sendZoc();
	}
	else if (e.data.cmd == "remove") {
		sendRemoveUnit(e.data.cord);
	}
};


function sendInfo(txt) {
	const info = {};
	info.message = txt;
	postMessage(["InfoMsg", info]);
}

function sendInitGame() {
	const initGame = {};
	initGame.worldHeight = 4;
	initGame.worldWidth = 4;
	initGame.sectionWidth = 11;
	initGame.sectionHeight = 11;
	postMessage(["InitGame", initGame]);
}

function sendAddUnit(team, col, row) {
	const addUnit = {
		unit: {
			team: team
		},
		cord: {
			col: col,
			row: row
		}
	};
	postMessage(["AddUnit", addUnit])
}

function sendRemoveUnit(cord) {
	const removeUnit = {
		cord: cord
	};
	postMessage(["RemoveUnit", removeUnit])
}

function sendViewInit() {
	const initView = {};
	initView.viewDefinition = {
		origo: { col: 0, row: 0 },
		width: 30,
		height: 21,
		mainSection: 0
	};
	initView.sectionVersion = {

	};
	initView.sectionBorders = {
		0: {
			borders: {
				North: {
					start: { col: 0, row: 0 },
					end: { col: 10, row: 0 }
				},
				South: {
					start: { col: 0, row: 10 },
					end: { col: 11, row: 10 }
				}
			}
		}
	};
	postMessage(["InitView", initView]);
}

function sendSnapshot() {
	const snap = {
		terrains: [
			{ at: { col: 3, row: 3 }, e: "Hill" },

			{ at: { col: 5, row: 5 }, e: "WaterNW" },
			{ at: { col: 6, row: 5 }, e: "WaterNN" },
			{ at: { col: 7, row: 5 }, e: "WaterNE" },

			{ at: { col: 5, row: 6 }, e: "WaterWW" },
			{ at: { col: 6, row: 6 }, e: "Water" },
			{ at: { col: 7, row: 6 }, e: "WaterEE" },

			{ at: { col: 5, row: 7 }, e: "Water" },
			{ at: { col: 6, row: 7 }, e: "Water" },
			{ at: { col: 7, row: 7 }, e: "Water" },

			{ at: { col: 15, row: 5 }, e: "WaterNW" },
			{ at: { col: 16, row: 5 }, e: "WaterNN" },
			{ at: { col: 17, row: 5 }, e: "WaterNE" },

			{ at: { col: 15, row: 6 }, e: "WaterWW" },
			{ at: { col: 16, row: 6 }, e: "Water" },
			{ at: { col: 17, row: 6 }, e: "WaterEE" },

			{ at: { col: 15, row: 7 }, e: "WaterWW" },
			{ at: { col: 16, row: 7 }, e: "Water" },
			{ at: { col: 17, row: 7 }, e: "WaterEE" },


		],
		bases: [
			{
				e:
				{
					area: [
						{ col: 4, row: 12 },
						{ col: 5, row: 12 },
						{ col: 6, row: 12 },
						{ col: 5, row: 11 },
						{ col: 5, row: 13 }
					],
					zoc: [
						{ col: 3, row: 12 }
					]
				}
			}
		],
		units: [
			{
				e: { team: "Black" },
				at: { col: 10, row: 10 }
			},
			{
				e: { team: "Red" },
				at: { col: 11, row: 10 }
			},
			{
				e: { team: "Blue" },
				at: { col: 12, row: 10 }
			}
		]
	};


	postMessage(["Snapshot", snap])
}

function sendZoc() {
	const zoc = {
		teamToCords: {
			"Red": [
				{ col: 3, row: 12 },
				{ col: 4, row: 11 },
				{ col: 4, row: 13 }
			]
		}
	};
	postMessage(["ZoneOfControl", zoc]);
}

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

async function sendTwo() {
	sendInfo('Server says...');
	await sleep(2000);
	sendInfo('Server repeats....')
}
const path = require('path')
const fs = require('fs')

const express = require('express')
const compression = require('compression')
const bodyParser = require('body-parser')
const serveIndex = require('serve-index')
const WebSocket = require('ws');

const app = express()
const expressWS = require('express-ws')(app)

app.use(compression({ threshold: 0 }))
app.use(bodyParser.json())
app.use('/public', express.static(__dirname + '/public'))
app.use('/public', serveIndex('public/', {'icons': true, 'view': 'details'}))


app.post('/record', function (req, res) {
	const device = req.body.device.replace(/\W/g, '_')
	const file = path.join(__dirname, 'public/' + device + '.tsv')
	const line = req.body.line

	console.log(line)

	fs.appendFile(file, line + "\n", function(err) {
		if(err) {
			return console.log(err);
		}
	})

	res.json({ result: 'OK' })


	// connect to waiting observer
	const observer = observers[device]

	// forward event
	if (observer) {
		if (observer.readyState === WebSocket.OPEN) {
			observer.send(line)
		} else {
			console.log("Lost observer for " + device)
			delete observers[device]
		}
	}
})


const observers = {}


app.ws('/listen', function(ws, req) {
	ws.on('message', function(msg) {
		const command = msg.split(' ')
		console.log(command)

		if (command[0] == 'PING') {
			ws.send('PONG')
			return
		}
		if (command[0] == 'CONNECT') {
			observers[command[1]] = ws
			ws.send("CONNECTED " + command[1])
			return
		}
	})

	ws.on('close', function(reasonCode, description) {
		console.log(reasonCode + " " + description + ": Lost observer")
	});
})


console.log("Listening on port 22148 ...")
app.listen(22148)

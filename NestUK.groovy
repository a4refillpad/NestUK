/**
 *  Nest Direct
 *
 * Original Author: dianoga7@3dgo.net
 * Modified for UK: greg.hesp@gmail.com
 *  Code: https://github.com/smartthings-users/device-type.nest
 *
 * INSTALLATION
 * =========================================
 * 1) Create a new device type (https://graph-eu01-euwest1.api.smartthings.com/ide/devices)
 *     Name: Nest
 *     Author: dianoga7@3dgo.net : tweaked by wayne :) Feb 2016
 *     Capabilities:
 *         Polling
 *         Relative Humidity Measurement
 *         Thermostat
 *         Temperature Measurement
 *         Presence Sensor
 *         Sensor
 *     Custom Attributes:
 *         temperatureUnit
 *     Custom Commands:
 *         away
 *         present
 *         setPresence
 *         heatingSetpointUp
 *         heatingSetpointDown
 *         setFahrenheit
 *         setCelsius
 *
 * 
 * 2) Create a new device (https://graph-eu01-euwest1.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: Nest (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 3) Update device preferences
 *     Click on the new device to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your information.
 *     To find your serial number, login to http://home.nest.com. Click on the thermostat
 *     you want to control. Under settings, go to Technical Info. Your serial number is
 *     the second item.
 *
 * 4) That's it, you're done.
 *
 * Copyright (C) 2013 Brian Steere <dianoga7@3dgo.net>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

preferences {
	input("username", "text", title: "Username", description: "Your Nest username (usually an email address)")
	input("password", "password", title: "Password", description: "Your Nest password")
	input("serial", "text", title: "Serial #", description: "The serial number of your thermostat")
}

// for the UI
metadata {
	definition (name: "NestUK", namespace: "a4refillpad", author: "Wayne") {
		capability "Polling"
		capability "Relative Humidity Measurement"
		capability "Thermostat"
		capability "Temperature Measurement"
		capability "Presence Sensor"
		capability "Sensor"

		command "away"
		command "present"
		command "setPresence"
		command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "setFahrenheit"
		command "setCelsius"

		attribute "temperatureUnit", "string"	
        	attribute "temperature", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL"){
			    attributeState("default", label:'${currentValue}°')
            }
            tileAttribute("device.temperature", key: "VALUE_CONTROL") {
            	attributeState("default", action: "levelUpDown")
                attributeState("VALUE_UP", action: "heatingSetpointUp")
                attributeState("VALUE_DOWN", action: "heatingSetpointDown")
            }
			tileAttribute("device.humidity", key:"SECONDARY_CONTROL"){
				attributeState("default", label:'${currentValue}%')
			}
            
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    				attributeState("idle", backgroundColor:"#44b621")
    				attributeState("heating", backgroundColor:"#ff871d")
            }
    
    			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    				attributeState("off", label:'${name}')
    				attributeState("heat", label:'${name}')
    				attributeState("auto", label:'${name}')
  			}
  			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
    				attributeState("default", label:'${currentValue}')
  			}
            
			main "temperature"
			details "temperature"
 
		}

		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 6, , inactiveLabel: false, range:"(15..26)") {
			state "setHeatingSetpoint", label:'Set temperature to', action:"thermostat.setHeatingSetpoint"
        }
            
		standardTile("presence", "device.presence", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "present", label:'Home', action:"away", icon: "https://raw.githubusercontent.com/a4refillpad/media/master/icon_nest_home.png"
			state "not present", label:'Away', action:"present", icon: "https://raw.githubusercontent.com/a4refillpad/media/master/icon_nest_away.png"
		}
        
	        standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "idle", action:"setCelsius", label:'${name}', icon: "https://raw.githubusercontent.com/a4refillpad/media/master/nest_thermostat_leaf_icon.jpg"
			state "heating", action:"setCelsius", label:"heat", icon: "https://raw.githubusercontent.com/a4refillpad/media/master/nest_thermostat_heat_icon.jpg"
		}


		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}

		main(["temperature", "thermostatOperatingState", "humidity"])

		details(["temperature", "heatSliderControl", "presence", "thermostatOperatingState", "refresh", "temperature1"])

	}

}

// parse events into attributes
def parse(String description) {

}

// handle commands
def setHeatingSetpoint(temp) {
	def latestThermostatMode = device.latestState('thermostatMode')
	def temperatureUnit = device.latestValue('temperatureUnit')
    
    log.debug "Thermostat mode " + latestThermostatMode
	
	switch (temperatureUnit) {
		case "celsius":
			if (temp) {
				if (temp < 9) {
					temp = 9
				}
				if (temp > 32) {
					temp = 32
				}
				if (latestThermostatMode.stringValue == 'auto') {
					api('temperature', ['target_change_pending': true, 'target_temperature_low': temp]) {
						sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
					}
				} else if (latestThermostatMode.stringValue == 'heat') {
					api('temperature', ['target_change_pending': true, 'target_temperature': temp]) {
							sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
					}
				}
			}
			break;
		default:
			if (temp) {
				if (temp < 51) {
					temp = 51
				}
				if (temp > 89) {
					temp = 89
				}
				if (latestThermostatMode.stringValue == 'auto') {
					api('temperature', ['target_change_pending': true, 'target_temperature_low': fToC(temp)]) {
						sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
					}
				} else if (latestThermostatMode.stringValue == 'heat') {
					api('temperature', ['target_change_pending': true, 'target_temperature': fToC(temp)]) {
						sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
					}
				}
			}
			break;
	}
	poll()
}

def heatingSetpointUp(){
	double newSetpoint = device.currentValue("heatingSetpoint") + 0.1
	log.debug "Setting heat set point up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown(){
	double newSetpoint = device.currentValue("heatingSetpoint") - 0.1
	log.debug "Setting heat set point down to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def setFahrenheit() {
	def temperatureUnit = "fahrenheit"
	log.debug "Setting temperatureUnit to: ${temperatureUnit}"
	sendEvent(name: "temperatureUnit",   value: temperatureUnit)
	poll()
}

def setCelsius() {
	def temperatureUnit = "celsius"
	log.debug "Setting temperatureUnit to: ${temperatureUnit}"
	sendEvent(name: "temperatureUnit",   value: temperatureUnit)
	poll()
}

def off() {
	setThermostatMode('off')
}

def heat() {
	setThermostatMode('heat')
}

def emergencyHeat() {
	setThermostatMode('heat')
}

def auto() {
	setThermostatMode('range')
}

def setThermostatMode(mode) {
	mode = mode == 'emergency heat'? 'heat' : mode

	api('thermostat_mode', ['target_change_pending': true, 'target_temperature_type': mode]) {
		mode = mode == 'range' ? 'auto' : mode
		sendEvent(name: 'thermostatMode', value: mode)
		poll()
	}
}

def away() {
	setPresence('away')
	sendEvent(name: 'presence', value: 'not present')
}

def present() {
	setPresence('present')
	sendEvent(name: 'presence', value: 'present')
}

def setPresence(status) {
	log.debug "Status: $status"
	api('presence', ['away': status == 'away', 'away_timestamp': new Date().getTime(), 'away_setter': 0]) {
		poll()
	}
}

def poll() {
	log.debug "Executing 'poll'"
	api('status', []) {
		data.device = it.data.device.getAt(settings.serial)
		data.shared = it.data.shared.getAt(settings.serial)
		data.structureId = it.data.link.getAt(settings.serial).structure.tokenize('.')[1]
		data.structure = it.data.structure.getAt(data.structureId)

		data.device.fan_mode = data.device.fan_mode == 'duty-cycle'? 'circulate' : data.device.fan_mode
		data.structure.away = data.structure.away ? 'away' : 'present'

		log.debug(data.shared)

		def humidity = data.device.current_humidity
		def temperatureType = data.shared.target_temperature_type
		def heatingSetpoint = '--'
		def coolingSetpoint = '--'

		temperatureType = temperatureType == 'range' ? 'auto' : temperatureType

		sendEvent(name: 'humidity', value: humidity)
		sendEvent(name: 'thermostatFanMode', value: fanMode)
		sendEvent(name: 'thermostatMode', value: temperatureType)

		def temperatureUnit = device.latestValue('temperatureUnit')


		switch (temperatureUnit) {
			case "celsius":
				def temperature = 0.01*(Math.round(data.shared.current_temperature/0.01))
//				def temperature = 0.1*(Math.round(data.shared.current_temperature/0.1))-0.5
    
				def targetTemperature = 0.1*(Math.round(data.shared.target_temperature/0.1))


				if (temperatureType == "cool") {
					coolingSetpoint = targetTemperature
				} else if (temperatureType == "heat") {
					heatingSetpoint = targetTemperature
				} else if (temperatureType == "auto") {
					coolingSetpoint = Math.round(data.shared.target_temperature_high * 10)/10
					heatingSetpoint = Math.round(data.shared.target_temperature_low * 10)/10
				}

				sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit, state: temperatureType)
				sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
				sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
				break;
			default: 
				def temperature = 0.1*(Math.round((cToF(data.shared.current_temperature))/0.1))
				def targetTemperature = 0.5*(Math.round((cToF(data.shared.target_temperature))/0.5))

				if (temperatureType == "cool") {
					coolingSetpoint = targetTemperature
				} else if (temperatureType == "heat") {
					heatingSetpoint = targetTemperature
				} else if (temperatureType == "auto") {
					coolingSetpoint = Math.round((cToF(data.shared.target_temperature_high))*10)/10
					heatingSetpoint = Math.round((cToF(data.shared.target_temperature_low))*10)/10
				}

				sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit, state: temperatureType)
				sendEvent(name: 'coolingSetpoint', value: coolingSetpoint, unit: temperatureUnit, state: "cool")
				sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: temperatureUnit, state: "heat")
				break;
			}

		switch (device.latestValue('presence')) {
			case "present":
				if (data.structure.away == 'away') {
					sendEvent(name: 'presence', value: 'not present')
				}
				break;
			case "not present":
				if (data.structure.away == 'present') {
					sendEvent(name: 'presence', value: 'present')
				}
				break;
		}

		if (data.shared.hvac_ac_state) {
			sendEvent(name: 'thermostatOperatingState', value: "cooling")
		} else if (data.shared.hvac_heater_state) {
			sendEvent(name: 'thermostatOperatingState', value: "heating")
		} else if (data.shared.hvac_fan_state) {
			sendEvent(name: 'thermostatOperatingState', value: "fan only")
		} else {
			sendEvent(name: 'thermostatOperatingState', value: "idle")
		}
	}
}

def api(method, args = [], success = {}) {
	if(!isLoggedIn()) {
		log.debug "Need to login"
		login(method, args, success)
		return
	}

	def methods = [
		'status': [uri: "/v2/mobile/${data.auth.user}", type: 'get'],
		'temperature': [uri: "/v2/put/shared.${settings.serial}", type: 'post'],
		'presence': [uri: "/v2/put/structure.${data.structureId}", type: 'post']
	]

	def request = methods.getAt(method)

	log.debug "Logged in"
	doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
	log.debug "Calling $type : $uri : $args"

	if(uri.charAt(0) == '/') {
		uri = "${data.auth.urls.transport_url}${uri}"
	}

	def params = [
		uri: uri,
		headers: [
			'X-nl-protocol-version': 1,
			'X-nl-user-id': data.auth.userid,
			'Authorization': "Basic ${data.auth.access_token}"
		],
		body: args
	]

	def postRequest = { response ->
		if (response.getStatus() == 302) {
			def locations = response.getHeaders("Location")
			def location = locations[0].getValue()
			log.debug "redirecting to ${location}"
			doRequest(location, args, type, success)
		} else {
			success.call(response)
		}
	}

	try {
		if (type == 'post') {
			httpPostJson(params, postRequest)
		} else if (type == 'get') {
			httpGet(params, postRequest)
		}
	} catch (Throwable e) {
		login()
	}
}

def login(method = null, args = [], success = {}) {
	def params = [
		uri: 'https://home.nest.com/user/login',
		body: [username: settings.username, password: settings.password]
	]

	httpPost(params) {response ->
		data.auth = response.data
		data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
		log.debug data.auth

		api(method, args, success)
	}
}

def isLoggedIn() {
	if(!data.auth) {
		log.debug "No data.auth"
		return false
	}

	def now = new Date().getTime();
	return data.auth.expires_in > now
}

def cToF(temp) {
	return (temp * 1.8 + 32).toDouble()
}

def fToC(temp) {
	return ((temp - 32) / 1.8).toDouble()
}


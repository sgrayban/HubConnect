/**
 * HubConnect Server Instance
 *
 * Copyright 2019 Steve White, Retail Media Concepts LLC.
 *
 * HubConnect for Hubitat is a software package created and licensed by Retail Media Concepts LLC.
 * HubConnect, along with associated elements, including but not limited to online and/or electronic documentation are
 * protected by international laws and treaties governing intellectual property rights.
 *
 * This software has been licensed to you. All rights are reserved. You may use and/or modify the software.
 * You may not sublicense or distribute this software or any modifications to third parties in any way.
 *
 * By downloading, installing, and/or executing this software you hereby agree to the terms and conditions set forth in the HubConnect license agreement.
 * <http://irisusers.com/hubitat/hubconnect/HubConnect_License_Agreement.html>
 * 
 * Hubitat is the trademark and intellectual property of Hubitat, Inc. Retail Media Concepts LLC has no formal or informal affiliations or relationships with Hubitat.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License Agreement
 * for the specific language governing permissions and limitations under the License.
 *
 */
import groovy.transform.Field
import groovy.json.JsonSlurper
definition(
	name: "HubConnect Server Instance",
	namespace: "shackrat",
	author: "Steve White",
	description: "Synchronizes devices and events across multiple hubs..",
	category: "My Apps",
	parent: "shackrat:HubConnect Server for Hubitat",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png"
)


// Preference pages
preferences
{
	page(name: "mainPage")
	page(name: "connectPage")
	page(name: "devicePage")
	page(name: "sensorsPage")
	page(name: "shackratsDriverPage")
	page(name: "switchDimmerBulbPage")
	page(name: "safetySecurityPage")
	page(name: "otherDevicePage")
	page(name: "customDevicePage")
}


// Map containing driver and attribute definitions for each device class
@Field NATIVE_DEVICES =
[
	"arlocamera":		[driver: "Arlo Camera", selector: "arloProCameras", attr: ["switch", "motion", "sound", "rssi", "battery"]],
	"arloqcamera":		[driver: "Arlo Camera", selector: "arloQCameras", attr: ["switch", "motion", "sound", "rssi", "battery"]],
	"arrival":			[driver: "Arrival Sensor", selector: "smartThingsArrival", attr: ["presence", "battery", "tone"]],
	"button":			[driver: "Button", selector: "genericButtons", attr: ["numberOfButtons", "pushed", "held", "doubleTapped", "button", "temperature", "battery"]],
	"contact":			[driver: "Contact Sensor", selector: "genericContacts", attr: ["contact", "temperature", "battery"]],
	"dimmer":			[driver: "Dimmer", selector: "genericDimmers", attr: ["switch", "level"]],
	"domemotion":		[driver: "Dome Motion Sensor", selector: "domeMotions", attr: ["motion", "temperature", "illuminance", "battery"]],
	"energyplug":		[driver: "DomeAeon Plug", selector: "energyPlugs", attr: ["switch", "power", "voltage", "current", "energy", "acceleration"]],
	"garagedoor":		[driver: "Garage Door", selector: "garageDoors", attr: ["door", "contact"]],
	"irissmartplug":	[driver: "Iris Smart Plug", selector: "smartPlugs", attr: ["switch", "power", "voltage", "ACFrequency"]],
	"irisv3motion":		[driver: "IrisV3 Motion Sensor", selector: "irisV3Motions", attr: ["motion", "temperature", "humidity", "battery"]],
	"keypad":			[driver: "Keypad", selector: "genericKeypads", attr: ["motion", "temperature", "battery", "tamper", "alarm"]],
	"lock":				[driver: "Lock", selector: "genericLocks", attr: ["lock", "lockCodes", "codeChanged", "codeLength", "maxCodes", "battery"]],
	"moisture":			[driver: "Moisture Sensor", selector: "genericMoistures", attr: ["water", "temperature", "battery"]],
	"motion":			[driver: "Motion Sensor", selector: "genericMotions", attr: ["motion", "temperature", "battery"]],
	"multipurpose":		[driver: "Multipurpose Sensor", selector: "genericMultipurposes", attr: ["contact", "temperature", "battery", "acceleration", "threeAxis"]],
	"omnipurpose":		[driver: "Omnipurpose Sensor", selector: "genericOmnipurposes", attr: ["motion", "temperature", "humidity", "illuminance", "ultravioletIndex", "tamper", "battery"]],
	"pocketsocket":		[driver: "Pocket Socket", selector: "pocketSockets", attr: ["switch", "power"]],
	"power":			[driver: "Power Meter", selector: "powerMeters", attr: ["power"]],
	"presence":			[driver: "Presence Sensor", selector: "genericPresences", attr: ["presence", "battery"]],
	"ringdoorbell":		[driver: "Ring Doorbell", selector: "ringDoorbellPros", attr: ["numberOfButtons", "pushed", "motion"]],
	"rgbbulb":			[driver: "RGB Bulb", selector: "genericRGBs", attr: ["switch", "level", "hue", "saturation", "RGB", "color", "colorMode", "colorTemperature"]],
	"shock":			[driver: "Shock Sensor", selector: "genericShocks", attr: ["shock", "battery"]],
	"siren":			[driver: "Siren", selector: "genericSirens", attr: ["switch", "alarm", "battery"]],
	"smartsmoke":		[driver: "Smart Smoke/CO", selector: "smartSmokeCO", attr: ["smoke", "carbonMonoxide", "battery", "temperature", "humidity", "switch", "level", "hue", "saturation", "pressure"]],
	"smoke":			[driver: "Smoke/CO Detector", selector: "genericSmokeCO", attr: ["smoke", "carbonMonoxide", "battery"]],
	"switch":			[driver: "Switch", selector: "genericSwitches", attr: ["switch"]],
	"thermostat":		[driver: "Thermostat", selector: "genericThermostats", attr: ["coolingSetpoint", "heatingSetpoint", "schedule", "supportedThermostatFanModes", "supportedThermostatModes", "temperature", "thermostatFanMode", "thermostatMode", "thermostatOperatingState", "thermostatSetpoint"]],
	"windowshade":		[driver: "Window Shade", selector: "windowShades", attr: ["switch", "position", "windowShade"]],
	"zwaverepeater":	[driver: "Iris Z-Wave Repeater", selector: "zwaveRepeaters", attr: ["status", "lastRefresh", "deviceMSR", "lastMsgRcvd"]]
]


// Mapping to receive events
mappings
{
	// Server Mappings
    path("/devices/save")
	{
		action: [POST: "saveDevices"]
	}
    path("/device/:deviceId/event/:event")
	{
		action: [GET: "deviceEvent"]
	}
    path("/ping")
	{
		action: [GET: "registerPing"]
	}
    path("/connect/:data")
	{
		action: [GET: "connectRemoteHub"]
	}
	

	// Client mappings
    path("/event/:deviceId/:deviceCommand/:commandParams")
	{
		action: [GET: "remoteDeviceCommand"]
	}
    path("/device/:deviceId/sync/:type")
	{
		action: [GET: "syncRemoteDevice"]
	}
}


/*
	syncDevice
    
	Purpose: Sync virtual device on this hub with the physcial (remote) device by requesting an update of all attribute values.

	URL Format: GET /device/:deviceId/sync/:type

	Notes: CALLED FROM CHILD DEVICE DRIVER
*/
def syncDevice(deviceId, deviceType)
{
	def dniParts = deviceId.split(":")

	def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${deviceId}"}
	if (childDevice)
	{
		if (enableDebug) log.debug "Requesting device sync from ${clientName}: ${childDevice.label}"

		def data = httpGetWithReturn("/device/${dniParts[1]}/sync/${deviceType}")

		if (data?.status == "success")
		{
			childDevice.setLabel(data.label)
			
			data?.currentValues.each
			{
			  attr ->
				childDevice.sendEvent([name: attr.name, value: attr.value, unit: attr.unit, descriptionText: "Sync: ${childDevice.displayName} ${attr.name} is ${attr.value} ${attr.unit}", isStateChange: true])
			}
		}
	}
}


/*
	sendDeviceEvent
    
	Purpose: Send an event to a client device.

	URL format: GET /event/:deviceId/:deviceCommand/:commandParams

	Notes: CALLED FROM CHILD DEVICE
*/
def sendDeviceEvent(deviceId, deviceCommand, List commandParams=[])
{
	if (state.commDisabled) return

	def dniParts = deviceId.split(":")
	def paramsEncoded = commandParams ? URLEncoder.encode(new groovy.json.JsonBuilder(commandParams).toString()) : null
	sendGetCommand("/event/${dniParts[1]}/${deviceCommand}/${paramsEncoded}")
}


/*
	deviceEvent
    
	Purpose: Handler for events received from physical devices on remote hubs.

	URL Format: (GET) /device/:deviceId/event/:event

	Notes: 	Buttons require the state change flag to be set to true in order for events to fire.
			(For other types, pass value through as-is.)
*/
def deviceEvent()
{
	def eventraw = params.event ? URLDecoder.decode(params.event) : null
	if (eventraw == null) return

	def event = new JsonSlurper().parseText(new String(eventraw))
	def data = event?.data ?: ""
	def unit = event?.unit ?: ""

	def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${clientIP}:${params.deviceId}"}
	if (childDevice)
	{
		if (enableDebug) log.info "Received event from ${clientName}/${childDevice.label}: [${event.name}, ${event.value} ${unit}, isStateChange: ${event.isStateChange}]"
		childDevice.sendEvent([name: event.name, value: event.value, unit: unit, descriptionText: "${childDevice.displayName} ${event.name} is ${event.value} ${unit}", isStateChange: event.isStateChange, data: data])
	}
	else if (enableDebug) log.warn "Ignoring Received event from ${clientName}: Device Not Found!"
}


/*
	wsSendEvent
    
	Purpose: Handler for events received from physical devices through the websocket interface.

	Notes: 	This is only called by the hub device for events received through its local websocket.
			Also, this does not warn when a device cannot be found as websockets get ALL events.
*/
def wsSendEvent(event)
{
	def childDevice = getChildDevices()?.find { it.deviceNetworkId == "${clientIP}:${event.deviceId}"}
	if (childDevice)
	{
		if (enableDebug) log.info "Received event from ${clientName}/${childDevice.label}: [${event.name}, ${event.value} ${event.unit}]"
		childDevice.sendEvent([name: event.name, value: event.value, unit: event.unit, descriptionText: event.descriptionText, isStateChange: true])
	}
}


/*
	realtimeModeChangeHandler
    
	URL Format: GET /modes/modeName

	Purpose: Event handler for mode change events on the controller hub (this one).
*/
def realtimeModeChangeHandler(evt)
{
	if (!pushModes) return
	def newMode = evt.value

	if (enableDebug) log.debug "Sending mode change event to ${clientName}: ${newMode}"
	sendGetCommand("/modes/${URLEncoder.encode(newMode)}")
}


/*
	saveDevices
    
	Purpose: Creates virtual, linked devices as received from the remote client hub.

	URL Format: (POST) /devices/save

	Notes: 	Thank god this isn't SmartThings, or this would time out after creating three devices!
*/
def saveDevices()
{	
	// Find the device class
	if (!request?.JSON?.deviceclass || !request?.JSON?.devices)
	{
		return jsonResponse([status: "error"])
	}

	if (NATIVE_DEVICES.find {it.key == request.JSON.deviceclass})
	{
		// Create the devices
		request.JSON.devices.each { createLinkedChildDevice(it, "HubConnect ${NATIVE_DEVICES[request.JSON.deviceclass].driver}") }
	}
	else if (state.customDrivers.find {it.key == request.JSON.deviceclass})
	{
		// Get the custom device type and create the devices
		request.JSON.devices.each { createLinkedChildDevice(it, "${state.customDrivers[request.JSON.deviceclass].driver}") }		
	}

	jsonResponse([status: "complete"])
}


/*
	createLinkedChildDevice
    
	Purpose: Helper function to create child devices. 

	Notes: 	Called from saveDevices()
*/
private createLinkedChildDevice(dev, driverType)
{
	def childDevice = getChildDevices()?.find{it.deviceNetworkId == "${clientIP}:${dev.id}"}
	if (childDevice)
	{
		// Device exists
		if (enableDebug) log.trace "${driverType} ${dev.label} exists... Skipping creation.."
		return
	}
	else
	{
		if (enableDebug) log.trace "Creating Device ${driverType} - ${dev.label}... ${clientIP}:${dev.id}..."
		try
		{
			childDevice = addChildDevice("shackrat", driverType, "${clientIP}:${dev.id}", null, [name: dev.label, label: dev.label])
		}
		catch (errorException)
		{
			log.error "... Uunable to create device ${dev.label}: ${errorException}."
			childDevice = null
		}
	}

	// Set the value of the primary attributes
	if (childDevice)
	{
		dev.attr.each
		{
	 	 attribute ->
			childDevice.sendEvent([name: attribute.name, value: attribute.value, unit: attribute.unit])
		}
	}
}


/*
	createHubChildDevice
    
	Purpose: Create child device for the remote hub so up/down status can be managed with rules.

	Notes: 	Called from initialize()
*/
private createHubChildDevice()
{
	def hubDevice = getChildDevices()?.find{it.deviceNetworkId == "hub-${clientIP}"}
	if (hubDevice)
	{
		// Hub exists
		log.error "Hub device exists... Skipping creation.."
		hubDevice = null
	}
	else
	{
		if (enableDebug) log.trace "Creating hub Device ${clientName}... hub-${clientIP}..."
		
		try
		{
			hubDevice = addChildDevice("shackrat", "HubConnect Remote Hub", "hub-${clientIP}", null, [name: "HubConnect Hub", label: clientName])
		}
		catch (errorException)
		{
			log.error "Unable to create the Hub monitoring device: ${errorException}.   Support Data: [id: \"hub-${clientIP}\", name: \"HubConnect Hub\", label: \"${clientName}\"]"
			hubDevice = null
		}
		
		// Set the value of the primary attributes
		if (hubDevice) sendEvent("hub-${clientIP}", [name: "presence", value: "present"])
	}

	hubDevice
}


/*
	syncRemoteDevice
    
	Purpose: Retrieves the physical (local) device details and returns them to the client (remote) hub.

	Notes: Called from HTTP request from client (remote) hub.
*/
def syncRemoteDevice()
{
	log.info "Received device update request from client: [${params.deviceId}, type ${params.type}]"
	
	def device = getDevice(params)
	if (device)
	{
		def currentAttributes = getAttributeMap(device, params.type)	
		def label = device.label ?: device.name
		jsonResponse([status: "success", name: "${device.name}", label: "${label}", currentValues: currentAttributes])
	}
}


/*
	getDevice
    
	Purpose: Helper function to retreive a specific device from all selected devices. 
*/
def getDevice(params)
{
	def foundDevice = null

	NATIVE_DEVICES.each
	{
	  groupname, device ->
		if (foundDevice != null) return
		foundDevice = settings."${device.selector}"?.find{it.id == params.deviceId}
	}

	// Custom devices drivers
	if (foundDevice == null)
	{
		state.customDrivers?.each
		{
	 	  groupname, driver ->
			if (foundDevice != null) return
			foundDevice = settings."custom_${groupname}".find{it.id == params.deviceId}
		}
	}
	return foundDevice
}


/*
	remoteDeviceCommand
    
	Purpose: Event handler for remote hub device events.

	URL format: GET /event/:deviceId/:deviceCommand/:commandParams

	Notes: Called from HTTP request from client (remote) hub.
*/
def remoteDeviceCommand()
{
	def commandParams = params.commandParams ? new JsonSlurper().parseText(new String(URLDecoder.decode(params.commandParams))) : null

	// Get the device
	def device = getDevice(params)
	if (!device)
	{
		log.error "Could not locate a device with an id of ${device.deviceId}"
		return jsonResponse([status: "error"])
	}
	
	if (enableDebug) log.info "Received command from client: [\"${device.label ?: device.name}\": ${params.deviceCommand}]"
	
	// Make sure the physical device supports the command
	if (device.supportedCommands.find{it.toString() == params.deviceCommand} == null)
	{
		log.error "The device [${device.label ?: device.name}] does not support the command ${params.deviceCommand}."
		return jsonResponse([status: "error"])
	}

	// Handle remaining commands
	else if (params.deviceCommand != "")
	{
		switch (commandParams?.size())
		{
			case 1:
				device."${params.deviceCommand}"(commandParams[0])
				break
			case 2:
				device."${params.deviceCommand}"(commandParams[0], commandParams[1])
				break
			case 3:
				device."${params.deviceCommand}"(commandParams[0], commandParams[1], commandParams[2])
				break
			case 4:
				device."${params.deviceCommand}"(commandParams[0], commandParams[1], commandParams[2], commandParams[3])
				break
			default:
				device."${params.deviceCommand}"()
				break
		}
	}

	else
	{
		log.error "Could not locate a device or command."
		return jsonResponse([status: "error"])
	}
	
	jsonResponse([status: "success"])
}


/*
	subscribeLocalEvents
    
	Purpose: Subscribes to all device events for all attribute returned by getSupportedAttributes()

	Notes: 	Thank god this isn't SmartThings, or this would time out after about 10 subscriptions!

*/
def subscribeLocalEvents()
{
	log.info "Subscribing to device events.."
	unsubscribe()

	NATIVE_DEVICES.each
	{
	  groupname, device ->
		def selectedDevices = settings."${device.selector}"
		if (selectedDevices?.size()) getSupportedAttributes(groupname).each { subscribe(selectedDevices, it, realtimeEventHandler) }
	}

	// Special handling for Smart Plugs & Power Meters - Kinda Kludgy
	if (!sp_EnablePower && smartPlugs?.size()) unsubscribe(smartPlugs, "power", realtimeEventHandler)
	if (!sp_EnableVolts && smartPlugs?.size()) unsubscribe(smartPlugs, "voltage", realtimeEventHandler)
	if (!pm_EnableVolts && powerMeters?.size()) unsubscribe(powerMeters, "voltage", realtimeEventHandler)

	// Custom defined drivers
	state.customDrivers?.each
	{
	  groupname, driver ->
		if (settings."custom_${groupname}"?.size()) getSupportedAttributes(groupname).each { subscribe(settings."custom_${groupname}", it, realtimeEventHandler) }	
	}
}


/*
	realtimeEventHandler
    
	Purpose: Event handler for all local device events.

	URL Format: GET /device/:deviceId/sync/:type

	Notes: Send local events to the remote hub. (Filters out urlencoded Degree symbols foir ST)
*/
def realtimeEventHandler(evt)
{
	if (state.commDisabled) return

	def event =
	[
		name:			evt.name,
		value:			evt.value,
		unit:			remoteType != "smartthings" ? evt.unit : evt.unit?.replace("°", ""),
		isStateChange:	evt.isStateChange,
		data:			evt.data
	]
	
	def data = URLEncoder.encode(new groovy.json.JsonBuilder(event).toString())

	if (enableDebug) log.debug "Sending event to server: ${evt.device.label ?: evt.device.name} [${evt.name}: ${evt.value} ${evt.unit}]"
	sendGetCommand("/device/${evt.deviceId}/event/${data}")
}


/*
	getAttributeMap
    
	Purpose: Returns a map of current attribute values for (device) with the device class (deviceClass).

	Notes: Calls getSupportedAttributes() to obtain list of attributes.
*/
def getAttributeMap(device, deviceClass)
{
	def deviceAttributes = getSupportedAttributes(deviceClass)
	def currentAttributes = []
	deviceAttributes.each
	{
		if (device.supportedAttributes.find{attr -> attr.toString() == it})  // Filter only attributes the device supports
			currentAttributes << [name: "${it}", value: device.currentValue("${it}"), unit: it == "temperature" ? "°"+getTemperatureScale() : it == "power" ? "W" :  it == "voltage" ? "V" : ""]
	}
	return currentAttributes
}


/*
	getSupportedAttributes
    
	Purpose: Returns a list of supported attribute values for the device class (deviceClass).

	Notes: Called from getAttributeMap().
*/
private getSupportedAttributes(deviceClass)
{
	if (NATIVE_DEVICES.find{it.key == deviceClass}) return NATIVE_DEVICES[deviceClass].attr
	if (state.customDrivers.find{it.key == deviceClass}) return state.customDrivers[deviceClass].attr
	return null
}


/*
	saveDevicesToClient
    
	Purpose: Sends all of the devices selected (& current attribute values) from this hub to the client (remote) hub.

	URL Format: POST /devices/save

	Notes: Makes a single POST request for each group of devices.
*/
def saveDevicesToClient()
{
	if (state.saveDevices == false) return

	// Fetch all devices and attributes for each device group and send them to the master.
	NATIVE_DEVICES.each
	{
	  groupname, device ->

		def devices = []
		settings."${device.selector}".each
		{
			log.debug getAttributeMap(it, groupname)
			devices << [id: it.id, label: it.label ?: it.name, attr: getAttributeMap(it, groupname)]
		}
		if (devices != [])
		{
			if (enableDebug) log.info "Sending devices to remote: ${groupname} - ${devices}"
			sendPostCommand("/devices/save", [deviceclass: groupname, devices: devices])
		}
	}

	// Custom defined device drivers
	state.customDrivers.each
	{
	  groupname, driver ->
		def customSel = settings?."custom_${groupname}"
		if (customSel != null)
		{
			if (enableDebug) log.info "Sending custom devices to remote..."
			sendPostCommand("/devices/save", [deviceclass: groupname, devices: customSel])
		}
	}
	state.saveDevices = false
}


/*
	httpGetWithReturn
    
	Purpose: Helper function to format GET requests with the proper oAuth token.

	Notes: 	Command is absolute and must begin with '/'
			Returns JSON Map if successful.
*/
def httpGetWithReturn(command)
{
	def serverURI = state.clientURI + command

	def requestParams =
	[
		uri:  serverURI,
		requestContentType: "application/json",
		headers:
		[
			Authorization: "Bearer ${state.clientToken}"
		]
	]
    
	httpGet(requestParams)
	{
	  response ->
		if (response?.status == 200)
		{
			return response.data
		}
		else
		{
			log.error "httpGet() request failed with error ${response?.status}"
		}
	}
}


/*
	sendGetCommand
    
	Purpose: Helper function to format GET requests with the proper oAuth token.

	Notes: 	Executes async http request and does not return data.
*/
def sendGetCommand(command)
{
	def serverURI = state.clientURI + command

	def requestParams =
	[
		uri:  serverURI,
		requestContentType: "application/json",
		headers:
		[
			Authorization: "Bearer ${state.clientToken}"
		]
	]
    
	asynchttpGet("asyncHTTPHandler", requestParams)
}


/*
	asyncHTTPHandler
    
	Purpose: Helper function to handle returned data from asyncHttpGet.

	Notes: 	Does not return data, only logs errors.
*/
def asyncHTTPHandler(response, data)
{
	if (response?.status != 200)
	{
		log.error "httpGet() request failed with error ${response?.status}"
	}
}


/*
	sendPostCommand
    
	Purpose: Helper function to format POST requests with the proper oAuth token.

	Notes: 	Returns JSON Map if successful.
*/
def sendPostCommand(command, data)
{
	def clientURI = state.clientURI + command + "?access_token=" + state.clientToken

	def requestParams =
	[
		uri:  clientURI,
		requestContentType: "application/json",
		body: data
	]

	httpPostJson(requestParams)
	{
	  response ->
		if (response?.status == 200)
		{
			return response.data
		}
		else
		{
			log.error "httpPost() request failed with error ${response?.status}"
		}
	}
}


/*
	mainPage
    
	Purpose: Displays the main (landing) page.

	Notes: 	Not very exciting.
*/
def mainPage()
{
	if (settings?.clientName && app.label == "")
	{
		app.updateLabel(clientName)
	}
	
	def devicesConfigured = genericContacts?.size() ?: genericMultipurposes?.size() ?: genericOmnipurposes?.size() ?: genericMotions?.size() ?: genericShocks?.size()
		?: smartPlugs?.size() ?: zwaveRepeaters?.size()
		?: genericSwitches?.size() ?: genericDimmers?.size() ?: genericRGBs?.size() ?: pocketSockets?.size() ?: energyPlugs?.size() ?: powerMeters?.size()
		?: genericSmokeCO?.size() ?: smartSmokeCO?.size() ?: genericMoistures?.size() ?: genericKeypads?.size() ?: genericLocks?.size() ?: genericSirens?.size()
		?: genericPresences?.size() ?: smartThingsArrival?.size() ?: genericButtons?.size() ?: genericValves?.size() ?: garageDoors?.size() ?: windowShades?.size()

	dynamicPage(name: "mainPage", uninstall: true, install: true)
	{
		if (state.clientURI)
		{
			section("<h2>${app.label}</h2>"){}
		}
		section("-= <b>Main Menu</b> =-")
		{
			href "connectPage", title: "Connect to Client Hub...", description: "", state: state.clientURI ? "complete" : null
			if (state.clientURI)
			{
				href "devicePage", title: "Connect local devices to Client Hub...", description: "", state: devicesConfigured ? "complete" : null
				input "pushModes", "bool", title: "Push mode changes to Client Hub?", description: "", defaultValue: false
			}
		}
		section("-= <b>Debug Menu</b> =-")
		{
			input "enableDebug", "bool", title: "Enable debug output?", required: false, defaultValue: false
		}
		section("-= <b>HubConnect v${appVersion.major}.${appVersion.minor}</b> =-")
		{
			paragraph "<span style=\"font-size:.8em\">Server Instance v${appVersion.major}.${appVersion.minor}.${appVersion.build} ${appCopyright}</span>"
		}
	}
}


/*
	connectPage
    
	Purpose: Displays the local & remote oAuth links.

	Notes: 	Really should create a proper token exchange someday.
*/
def connectPage()
{
	if (!state?.accessToken)
	{
		createAccessToken()
	}

	def connectString = remoteType ? new groovy.json.JsonBuilder([uri: remoteType == "local" ? getFullLocalApiServerUrl() : getFullApiServerUrl(), type: remoteType, token: state.accessToken, connectionType: localConnectionType ?: ""]).toString().bytes.encodeBase64() : ""

	dynamicPage(name: "connectPage", uninstall: false, install: false)
	{
		section("Client Details")
		{ 
			input "clientName", "string", title: "Friendly Name of Client Hub:", required: false, defaultValue: null, submitOnChange: true
			if (clientName) input "clientIP", "string", title: "Private LAN IP Address of Client Hub:", required: false, defaultValue: null, submitOnChange: true
			if (clientIP) input "remoteType", "enum", title: "Type of Remote Hub:", options: [local: "Hubitat (LAN)", remote: "Hubitat (Internet)", smartthings: "SmartThings"], required: false, defaultValue: null, submitOnChange: true			
			if (remoteType == "local") input "localConnectionType", "enum", title: "Local connect type:", options: [http: "Hubitat oAuth (http)", socket: "Hubitat Event Socket"], required: false, defaultValue: null, submitOnChange: true	
		}
		if (remoteType)
		{
			section("Connection Key")
			{ 
				if (remoteType == "local") paragraph("Local LAN Hub: Copy &amp; Paste this Connection Key into the Remote hub's configuration: <input type=\"text\" size=\"100\" value=\"${connectString}\" />")
				else paragraph("Internet Hub: Copy &amp; Paste this Connection Key into the Remote hub's configuration: <input type=\"text\" size=\"100\" value=\"${connectString}\" />")
			}
		}
	}
}


/*
	connectRemoteHub
    
	Purpose: Receives URL and token from client (remote) hub.

	URL Format: GET /connect/:data

	Notes: 	This happens only after a successful installation of the server hubs connection key.
*/
def connectRemoteHub()
{
	if (params.data == null) return
	
	def accessData = new JsonSlurper().parseText(new String(params.data.decodeBase64()))
	if (!accessData || !accessData?.token || !accessData?.mac)
	{
		return jsonResponse([status: "error", message: "Invalid connect Key"])
	}

	if (state?.clientMac && (state.clientMac != accessData.mac))
	{
		return jsonResponse([status: "error", message: "Instance in use by another hub"])
	}
	
	log.info "Setting remote hub URI: ${accessData.uri} with token ${accessData.token}"
	state.clientURI = accessData.uri
	state.clientToken = accessData.token
	state.clientMac = accessData.mac
	
	jsonResponse([status: "success"])
}


/*
	registerPing
    
	Purpose: Handles a hub health ping event from a remote hub.

	Notes: 	If a hub was previously offline, the virtual hub device presence state will be set to "present".
*/
def registerPing()
{
	if (state.connectStatus == "warning" || state.connectStatus == "offline")
	{
		// A little recovery for system mode, in case the hub coming online missed a mode change
		if (pushModes)
		{	
			sendGetCommand("/modes/${URLEncoder.encode(location.mode)}")
		}

		state.connectStatus = "online"
		app.updateLabel(clientName + "<span style=\"color:green\"> Online</span>")
		log.info "${clientName} is online."

		sendEvent("hub-${clientIP}", [name: "presence", value: "present"])
	}
	
	if (enableDebug) log.trace "Received ping from ${clientName}."
	
	state.lastCheckIn = now()

	jsonResponse([status: "received"])	
}


/*
	appHealth
    
	Purpose: Scheduled health check task to ensure remote hubs are checking in regularly.

	Notes: 	Hubs are considered in a warning state after missing 2 pings (2 minutes).
			Hubs are considered offline after missing 5 pings (5 minutes).
			When a hub is offline, the virtual hub device presence state will be set to "not present".
*/
def appHealth()
{
	// There should always be an event at least every minute.  If it's been 5 minutes (4 missed pings, the hub may be offline.
	if (state.lastCheckIn < (now() - 300000)) // 5 minutes - offline
	{
		state.connectStatus = "offline"
		app.updateLabel(clientName + "<span style=\"color:red\"> Offline</span>")
		log.error "${clientName} is offline."

		sendEvent("hub-${clientIP}", [name: "presence", value: "not present"])
	}
	else if (state.lastCheckIn < (now() - 120000)) // 2 minutes - warning
	{
		state.connectStatus = "warning"
		app.updateLabel(clientName + "<span style=\"color:orange\"> Warning</span>")
		log.warn "${clientName} has missed a ping and may be offline."
	}
}


/*
	setCommStatus
    
	Purpose: Disables events communications between hubs.

	URL Format: /system/setCommStatus/true|false

	Notes: 	This is useful if the coordinator has to be rebooted to prevent HTTP errors on the remote hubs.
*/
def setCommStatus(commDisabled = false)
{
	log.info "Setting event communication status from remote hub: [status: ${commDisabled}]"
	state.commDisabled = commDisabled	
	response = httpGetWithReturn("/system/setCommStatus/${commDisabled}")
	sendEvent("hub-${clientIP}", [name: "switch", value: response.switch])
}


/*
	pushCurrentMode
    
	Purpose: Pushes the current mode out to remote hubs.

	URL Format: /modes/modeName

	Notes: Called by system start event to make sure the correct mode is pushed to all remote hubs.
*/
def pushCurrentMode()
{
	sendGetCommand("/modes/${URLEncoder.encode(location.mode)}")
}


/*
	saveCustomDrivers
    
	Purpose: Saves custom drivers defined in the parent app to this server instance.

	Notes: Called by parent app.
*/
def saveCustomDrivers(customDrivers)
{
	state.customDrivers = customDrivers
	sendPostCommand("/system/drivers/save", [customdrivers: customDrivers])
}


/*
	installed
    
	Purpose: Standard install function.

	Notes: Doesn't do much.
*/
def installed()
{
	log.info "${app.name} Installed"
	
	if (state.clientToken)
	{
		initialize()
	}
	else
	{
		// Set an error state
		state.lastCheckIn = 0
		state.connectStatus = "error"
	}
}


/*
	updated
    
	Purpose: Standard update function.

	Notes: Still doesn't do much.
*/
def updated()
{
	log.info "${app.name} Updated with settings: ${settings}"

	if (state?.customDrivers == null)
	{
		state.customDrivers = [:]
	}

	unsubscribe()
	initialize()
}


/*
	initialize
    
	Purpose: Initialize the server instance.

	Notes: Parses the oAuth link into the token and base URL.  A real token exchange would obviate the need for this.
*/
def initialize()
{
	log.info "${app.name} Initialized"
	
	saveDevicesToClient()
	subscribeLocalEvents()
	if (pushModes) subscribe(location, "mode", realtimeModeChangeHandler)
	
	state.lastCheckIn = now()
	state.connectStatus = "online"

	state.commDisabled = false

	def hubDevice = getChildDevices()?.find{it.deviceNetworkId == "hub-${clientIP}"}
	if (hubDevice)
	{
		hubDevice.setConnectionType(remoteType == "local" && localConnectionType == "socket" ? "socket" : "http")
	}
	else if (state.clientToken)
	{
		hubDevice = createHubChildDevice()
		hubDevice?.setConnectionType(remoteType == "local" && localConnectionType == "socket" ? "socket" : "http")
	}

	app.updateLabel(clientName + "<span style=\"color:green\"> Online</span>")
	runEvery5Minutes("appHealth")
}


/*
	jsonResponse
    
	Purpose: Helper function to render JSON responses
*/
def jsonResponse(respMap)
{
	render contentType: 'application/json', data: new groovy.json.JsonBuilder(respMap).toString()
}


/*
	devicePage
    
	Purpose: Displays the page where devices are selected to be linked to the controller hub.

	Notes: 	Really could stand to be better organized.
*/
def devicePage()
{
	def sensorsPageCount = genericContacts?.size() ?: genericMultipurposes?.size() ?: genericOmnipurposes?.size() ?: genericMotions?.size() ?: genericShocks?.size()
	def shackratsDriverPageCount = smartPlugs?.size() ?: zwaveRepeaters?.size()
	def switchDimmerBulbPageCount = genericSwitches?.size() ?: genericDimmers?.size() ?: genericRGBs?.size() ?: pocketSockets?.size() ?: powerMeters?.size()
	def safetySecurityPageCount = genericSmokeCO?.size() ?: smartSmokeCO?.size() ?: genericMoistures?.size() ?: genericKeypads?.size() ?: genericLocks?.size() ?: genericSirens?.size()
	def otherDevicePageCount = genericPresences?.size() ?: smartThingsArrival?.size() ?: genericButtons?.size() ?: genericThermostats?.size() ?: genericValves?.size() ?: garageDoors?.size() ?: windowShades?.size()

	def totalNativeDevices = 0
	def requiredDrivers = ""
	NATIVE_DEVICES.each
	{devicegroup, device ->
		if (settings."${device.selector}"?.size())
		{
			totalNativeDevices += settings."${device.selector}"?.size()
			requiredDrivers += "<li>HubConnect ${device.driver}</li>"
		}
	}

	def totalCustomDevices = 0
	state.customDrivers?.each
	{devicegroup, device ->
		totalCustomDevices += settings."${device.selector}"?.size() ?: 0
	}
	
	def totalDevices = totalNativeDevices + totalCustomDevices

	dynamicPage(name: "devicePage", uninstall: false, install: false)
	{
		section("<b> Select Devices to Link to Remote Hub ${clientName}</b>  (${totalDevices} connected)")
		{ 
			href "sensorsPage", title: "Sensors", description: "Contact, Motion, Multipurpose, Omnipurpose, Shock", state: sensorsPageCount ? "complete" : null
			href "shackratsDriverPage", title: "Shackrat's Drivers", description: "Iris Smart Plug, Z-Wave Repeaters", state: shackratsDriverPageCount ? "complete" : null
			href "switchDimmerBulbPage", title: "Switches & Dimmers", description: "Switch, Dimmer, Bulb, Power Meters", state: switchDimmerBulbPageCount ? "complete" : null
			href "safetySecurityPage", title: "Safety & Security", description: "Locks, Keypads, Smoke & Carbon Monoxide, Leak, Sirens", state: safetySecurityPageCount ? "complete" : null
			href "otherDevicePage", title: "Other Devices", description: "Presence, Button, Valves, Garage Doors, Window Shades", state: otherDevicePageCount ? "complete" : null
			href "customDevicePage", title: "Custom Devices", description: "Devices with user-defined drivers.", state: totalCustomDevices ? "complete" : null
		}
		if (requiredDrivers?.size())
		{
			section("<b>-= Required Drivers =-</b>")
			{
				paragraph "Please make sure the following native drivers are installed on the Remote hub before clicking \"Done\": <ul>${requiredDrivers}</ul>" 
			}
		}
	}
}


/*
	sensorsPage
    
	Purpose: Displays the page where sensor-type (motion, contact, etc.) devices are selected to be linked to the controller hub.

	Notes: 	First attempt at organization.
*/
def sensorsPage()
{
	state.saveDevices = true

	dynamicPage(name: "sensorsPage", uninstall: false, install: false)
	{
		section("<b>-= Select Contact Sensors (${genericContacts?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericContacts", "capability.contactSensor", title: "Contact Sensors (contact):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Multipurpose Sensors (${genericMultipurposes?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericMultipurposes", "capability.accelerationSensor", title: "Contact Multipurpose (contact, acceleration, threeAxis):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Omnipurpose Sensors (${genericOmnipurposes?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericOmnipurposes", "capability.relativeHumidityMeasurement", title: "Generic Omni-Sensor (contact, temperature, humidity, illuminance):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Motion Sensors (${genericMotions?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericMotions", "capability.motionSensor", title: "Motion Sensors (motion, temperature):", required: false, multiple: true, defaultValue: null
			input "irisV3Motions", "capability.motionSensor", title: "Motion Sensors (motion, temperature, humidity):", required: false, multiple: true, defaultValue: null
			input "domeMotions", "capability.motionSensor", title: "Motion Sensors (motion, temperature, illuminance):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Shock Sensors (${genericShocks?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericShocks", "capability.shockSensor", title: "Glass Break/Shock Sensors (shock):", required: false, multiple: true, defaultValue: null
		}
	}
}


/*
	shackratsDriverPage
    
	Purpose: Displays the page where shackrat's custom device drivers (SmartPlug, Z-Wave Repeater) are selected to be linked to the controller hub.

	Notes: 	First attempt at organization.
*/
def shackratsDriverPage()
{
	state.saveDevices = true

	dynamicPage(name: "shackratsDriverPage", uninstall: false, install: false)
	{
		section("<b>-= Select Smart Plug Devices (${smartPlugs?.size() ?: "0"} connected) =- </b>")
		{ 
			input "smartPlugs", "device.IrisSmartPlug", title: "Iris Smart Plugs (switch, power, voltage):", required: false, multiple: true, defaultValue: null
			input "sp_EnablePower", "bool", title: "Enable power meter reporting?", required: false, defaultValue: true
			input "sp_EnableVolts", "bool", title: "Enable voltage reporting?", required: false, defaultValue: true
		}
		section("<b>-= Select Z-Wave Plus Repeater Devices (${zwaveRepeaters?.size() ?: "0"} connected) =- </b>")
		{ 
			input "zwaveRepeaters", "device.IrisZ-WaveRepeater", title: "Z-Wave Repeaters (status):", required: false, multiple: true, defaultValue: null
		}
	}
}


/*
	switchDimmerBulbPage
    
	Purpose: Displays the page where switches, dimmers, bulbs, and power meters are selected to be linked to the controller hub.

	Notes: 	First attempt at organization.
*/
def switchDimmerBulbPage()
{
	state.saveDevices = true

	dynamicPage(name: "switchDimmerBulbPage", uninstall: false, install: false)
	{
		section("<b>-= Select Switch Devices (${genericSwitches?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericSwitches", "capability.switch", title: "Generic Switches (switch):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Dimmer Devices (${genericDimmers?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericDimmers", "capability.switchLevel", title: "Generic Dimmers (switch, level):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select RGB Bulbs (${genericRGBs?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericRGBs", "capability.colorControl", title: "Generic RGB Bulbs (switch, level, hue, saturation):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Zigbee Plugs/Pocket Sockets (${pocketSockets?.size() ?: "0"} connected) =-</b>")
		{ 
			input "pocketSockets", "capability.switch", title: "Pocket Sockets (switch, power):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Smart Plug Devices (${energyPlugs?.size() ?: "0"} connected) =- </b>")
		{ 
			input "energyPlugs", "capability.energyMeter", title: "Aeon/Dome Smart Plugs (switch, power, voltage, energy, acceleration, current):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Power Meter Devices (${powerMeters?.size() ?: "0"} connected) =-</b>")
		{ 
			input "powerMeters", "capability.powerMeter", title: "Power Meter Devices (power, voltage):", required: false, multiple: true, defaultValue: null
			input "pm_EnableVolts", "bool", title: "Enable voltage reporting?", required: false, defaultValue: true
		}
	}
}


/*
	safetySecurityPage
    
	Purpose: Displays the page where safety & security devices (smoke, carbonMonoxide, sirens, etc.) are selected to be linked to the controller hub.

	Notes: 	First attempt at organization.
*/
def safetySecurityPage()
{
	state.saveDevices = true

	dynamicPage(name: "safetySecurityPage", uninstall: false, install: false)
	{
		section("<b>-= Select Smoke and CO Detectors (${genericSmokeCO?.size() ?: "0"} connected) =-</b>")
		{
			input "genericSmokeCO", "capability.smokeDetector", title: "Smoke and CO Detectors (smoke, carbonMonoxide):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Smart Smoke and CO Detectors (${smartSmokeCO?.size() ?: "0"} connected) =-</b>")
		{
			input "smartSmokeCO", "device.HaloSmokeAlarm", title: "Halo Smoke and CO Detectors (smoke, carbonMonoxide, temperature, humidity, switch, level, pressure):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Moisture Sensors (${genericMoistures?.size() ?: "0"} connected) =-</b>")
		{
			input "genericMoistures", "capability.waterSensor", title: "Moisture Sensors (water):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Keypads (${genericKeypads?.size() ?: "0"} connected) =-</b>")
		{
			input "genericKeypads", "device.CentraliteKeypad", title: "Keypads (motion, temperature, tamper, alarm):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Locks (${genericLocks?.size() ?: "0"} connected) =-</b>")
		{
			input "genericLocks", "capability.lock", title: "Locks (lock):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Sirens (${genericSirens?.size() ?: "0"} connected) =-</b>")
		{
			input "genericSirens", "capability.alarm", title: "Sirens (switch, alarm):", required: false, multiple: true, defaultValue: null
		}
	}
}


/*
	otherDevicePage
    
	Purpose: Displays the page where other devices (presence, valves, etc.) are selected to be linked to the controller hub.

	Notes: 	First attempt at organization.
*/
def otherDevicePage()
{
	state.saveDevices = true

	dynamicPage(name: "otherDevicePage", uninstall: false, install: false)
	{
		section("<b>-= Select SmartThings Arrival Sensors (${smartThingsArrival?.size() ?: "0"} connected) =-</b>")
		{
			input "smartThingsArrival", "capability.presenceSensor", title: "SmartThings Arrival Sensors (presence, tone):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Presence Sensors (${genericPresences?.size() ?: "0"} connected) =-</b>")
		{
			input "genericPresences", "capability.presenceSensor", title: "Presence Sensors (presence):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Button Devices (${genericButtons?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericButtons", "capability.pushableButton", title: "Buttons (pushed, held, doubleTapped, released):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Thermostat Devices (${genericThermostats?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericThermostats", "capability.thermostat", title: "Thermostats (coolingSetpoint, heatingSetpoint, schedule, supportedThermostatFanModes, supportedThermostatModes, temperature, thermostatFanMode, thermostatMode, thermostatOperatingState, thermostatSetpoint):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Water Valve Devices (${genericValves?.size() ?: "0"} connected) =-</b>")
		{ 
			input "genericValves", "capability.valve", title: "Water Valves (valve):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Garage Doors (${garageDoors?.size() ?: "0"} connected) =-</b>")
		{
			input "garageDoors", "capability.garageDoorControl", title: "Garage Doors (door):", required: false, multiple: true, defaultValue: null
		}
		section("<b>-= Select Window Shades (${windowShades?.size() ?: "0"} connected) =-</b>")
		{
			input "windowShades", "capability.windowShade", title: "Window Shades:", required: false, multiple: true, defaultValue: null
		}
	}
}


/*
	customDevicePage
    
	Purpose: Displays the page where custom (user-defined) devices are selected to be linked to the controller hub.

	Notes: 	First attempt at remotely defined device definitions.
*/
def customDevicePage()
{
	state.saveDevices = true

	dynamicPage(name: "customDevicePage", uninstall: false, install: false)
	{
		state.customDrivers.each
		{
		  groupname, driver ->
			def customSel = settings."custom_${groupname}"
			section("<b>-= Select ${driver.driver} Devices (${customSel?.size() ?: "0"} connected) =-</b>")
			{
				input "custom_${groupname}", "capability.${driver.selector}", title: "${driver.driver} Devices (${driver.attr}):", required: false, multiple: true, defaultValue: null
			}
		}
	}
}

def getAppVersion() {[platform: "Hubitat", major: 1, minor: 2, build: 0]}
def getAppCopyright(){"&copy; 2019 Steve White, Retail Media Concepts LLC<br /><a href=\"https://github.com/shackrat/Hubitat-Private/blob/master/HubConnect/License%20Agreement.md\" target=\"_blank\">HubConnect License Agreement</a>"}
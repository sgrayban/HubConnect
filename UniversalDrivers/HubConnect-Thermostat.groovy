/*
 *	Copyright 2019 Steve White
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *	use this file except in compliance with the License. You may obtain a copy
 *	of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *	License for the specific language governing permissions and limitations
 *	under the License.
 *
 *
 */
 
metadata 
{
	definition(name: "HubConnect Thermostat", namespace: "shackrat", author: "Steve White", importUrl: "https://raw.githubusercontent.com/HubitatCommunity/HubConnect/master/UniversalDrivers/HubConnect-Thermostat.groovy")
	{
		capability "Sensor"
		capability "Thermostat"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"

		attribute "version", "string"    
		
		command "sync"
	}
}


/*
	installed
    
	Doesn't do much other than call initialize().
*/
def installed()
{
	initialize()
}


/*
	updated
    
	Doesn't do much other than call initialize().
*/
def updated()
{
	initialize()
}


/*
	initialize
    
	Doesn't do much other than call refresh().
*/
def initialize()
{

}


/*
	parse
    
	In a virtual world this should never be called.
*/
def parse(String description)
{
	log.trace "Msg: Description is $description"
}


/*
	auto
    
	Sets the thermostat operating mode to "auto".
*/
def auto()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "auto")
}


/*
	cool
    
	Sets the thermostat operating mode to "cool".
*/
def cool()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "cool")
}


/*
	emergencyHeat
    
	Turns on emergency heat.
*/
def emergencyHeat()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "emergencyHeat")
}


/*
	fanAuto
    
	Sets the fan operating mode to "auto".
*/
def fanAuto()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "fanAuto")
}


/*
	fanCirculate
    
	Sets the fan operating mode to "circulate".
*/
def fanCirculate()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "fanCirculate")
}


/*
	fanOn
    
	Sets the fan operating mode to "on".
*/
def fanOn()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "fanOn")
}


/*
	heat
    
	Sets the thermostat operating mode to "heat".
*/
def heat()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "heat")
}


/*
	off
    
	Sets the thermostat operating mode to "off".
*/
def off()
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "off")
}


/*
	setCoolingSetpoint
    
	Sets the cooling setpoint to <temperature>.
*/
def setCoolingSetpoint(temperature)
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "setCoolingSetpoint", [temperature])
}


/*
	setHeatingSetpoint
    
	Sets the heating setpoint to <temperature>.
*/
def setHeatingSetpoint(temperature)
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "setHeatingSetpoint", [temperature])
}


/*
	setSchedule
    
	Sets the thermostat schedule to <schedule> (JSON).
*/
def setSchedule(schedule)
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "setSchedule", [schedule.toString()])
}


/*
	setThermostatFanMode
    
	Sets the fans operating mode to <fanmode>.
*/
def setThermostatFanMode(fanmode)
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "setThermostatFanMode", [fanmode])
}


/*
	setThermostatMode
    
	Sets the thermostat operating mode to <thermostatmode>.
*/
def setThermostatMode(thermostatmode)
{
	// The server will update status
	parent.sendDeviceEvent(device.deviceNetworkId, "setThermostatMode", [thermostatmode])
}


/*
	sync
    
	Synchronizes the device details with the parent.
*/
def sync()
{
	// The server will respond with updated status and details
	parent.syncDevice(device.deviceNetworkId, "thermostat")
	sendEvent([name: "version", value: "v${driverVersion.major}.${driverVersion.minor}.${driverVersion.build}"])
}
def getDriverVersion() {[platform: "Universal", major: 1, minor: 2, build: 1]}
/**
 * HubConnect Server for Hubitat
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
definition(
	name: "HubConnect Server for Hubitat",
	namespace: "shackrat",
	author: "Steve White",
	description: "Synchronizes devices and events across hubs..",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-BigButtonsAndSwitches@2x.png"
)

// Preference pages
preferences
{
	page(name: "mainPage")
	page(name: "aboutPage")
	page(name: "customDriverPage")
	page(name: "createCustomDriver")
	page(name: "editCustomDriver")
	page(name: "saveCustomDriverPage")
}


// Map containing driver and attribute definitions for each device class
@Field ATTRIBUTE_TO_SELECTOR =
[
	"alarm":			"alarm",
	"battery":			"battery",
	"button":			"button",
	"bulb":				"bulb",
	"carbonMonoxide":	"carbonMonoxideDetector",
	"colorMode":		"colorMode",
	"colorTemperature":	"colorTemperature",
	"contact":			"contactSensor",
	"door":				"garageDoorControl",
	"doubleTapped":		"doubleTapableButton",
	"held":				"holdableButton",
	"illuminance":		"illuminanceMeasurement",
	"level":			"switchLevel",
	"lock":				"lock",
	"motion":			"motionSensor",
	"power":			"powerMeter",
	"pushed":			"pushableButton",
	"presence":			"presenceSensor",
	"refresh":			"refresh",
	"securityKeypad":	"securityKeypad",
	"shock":			"shockSensor",
	"smoke":			"smokeDetector",
	"speed":			"fanControl",
	"switch":			"switch",
	"temperature":		"temperatureMeasurement",
	"thermostat":		"thermostat",
	"valve":			"valve",
	"water":			"waterSensor",
	"windowshade":		"windowShade"
]


/*
	mainPage
    
	Purpose: Displays the main (landing) page.

	Notes: 	Not very exciting.
*/
def mainPage()
{
	dynamicPage(name: "mainPage", uninstall: true, install: true)
	{
		section("<h2>${app.label}</h2>"){}
		section
		{
			app(name: "hubClients", appName: "HubConnect Server Instance", namespace: "shackrat", title: "Connect a Hub...", multiple: true)
		}
		section("-= <b>Custom Drivers</b> =-")
		{
			href "customDriverPage", title: "Manage Custom Drivers", required: false
		}
		section("-= <b>Communications</b> =-")
		{
			input "haltComms", "bool", title: "Suspend all communications between this server and all remote hubs?  (Resets at reboot)", required: false, defaultValue: false
		}
		section("-= <b>HubConnect ${currentVersion}</b> =-")
		{
			href "aboutPage", title: "Help Support HubConnect!", description: "HubConnect is provided free of charge for the benefit the Hubitat community.  If you find HubConnect to be a valuable tool, please help support the project."
			paragraph "<span style=\"font-size:.8em\">Server Container build ${moduleBuild} ${appCopyright}</span>"
		}
	}
}


/*
	customDriverPage
    
	Purpose: Displays the page where shackrat's custom device drivers (SmartPlug, Z-Wave Repeater) are selected to be linked to the controller hub.

	Notes: 	First attempt at organization.
*/
def customDriverPage()
{
	dynamicPage(name: "customDriverPage", uninstall: false, install: false, nextPage: "mainPage")
	{
		section("Currently Installed Custom Drivers")
		{
			state.customDrivers.each
			{
			  groupname, driver ->
				href "editCustomDriver", title: "${driver.driver} (${groupname})", description: "${driver.attr}", params: [groupname: groupname]
			}

			href "createCustomDriver", title: "Add Driver", description: "Define a custom driver type.", params: [newdriver: true]
		}
	}
}


/*
	editCustomDriver
    
	Purpose: Loads the custom driver details into preferences so they can be edited in the UI.

	Notes: 	This requires a page refresh in order to work properly.  Hubitat cache issue??
*/
def editCustomDriver(params)
{
	if (params?.groupname)
	{
		def dtMap = state?.customDrivers[params.groupname]
		if (dtMap)
		{
			app.updateSetting("newDev_AttributeGroup", [type: "string", value: params.groupname])
			app.updateSetting("newDev_DriverName", [type: "string", value: dtMap.driver])

			dtMap.attr.eachWithIndex
			{
			  attr, idx ->
				app.updateSetting("attr_${idx+1}", [type: "string", value: attr])
			}
		}
		params.editgroup = params.groupname
		params.groupname = null
		dynamicPage(name: "editCustomDriver", uninstall: false, install: false, refreshInterval: 1)
		{
			section("Loading, please wait..."){}
		}
	}
	else driverPage("editCustomDriver", params?.editgroup)
}


/*
	createCustomDriver
    
	Purpose: Clears out all preferences so the UI page displays as if it were new.

	Notes: 	This requires a page refresh in order to work properly.  Hubitat cache issue??
*/
def createCustomDriver(params)
{
	if (params?.newdriver == true)
	{
		app.updateSetting("newDev_AttributeGroup", [type: "string", value: ""])
		app.updateSetting("newDev_DriverName", [type: "string", value: ""])
		app.updateSetting("attr_1", [type: "enum", value: ""])
		7.times
		{
			app.updateSetting("attr_${it+1}", [type: "string", value: ""])
		}
		params.newdriver = false
	}
	
	driverPage("createCustomDriver")
}


/*
	createCustomDriver
    
	Purpose: Displays the page where shackrat's custom device drivers (SmartPlug, Z-Wave Repeater) are selected to be linked to the controller hub.

	Notes: 	First attempt at organization.
*/
def driverPage(pageName, groupName = null)
{
	dynamicPage(name: pageName, uninstall: false, install: false)
	{
		section("<b>-= Configure Driver =- </b>")
		{ 
			if (groupName) paragraph "Attribute Class Name (letters & numbers only):<br />${groupName}"
			else input "newDev_AttributeGroup", "text", title: "Attribute Class Name (letters & numbers only):", required: true, defaultValue: null
			input "newDev_DriverName", "text", title: "Device Driver Name:", required: true, defaultValue: null, submitOnChange: true
		}
		if (newDev_AttributeGroup?.size() && newDev_DriverName?.size())
		{
			section("<b>-= Supported Attributes =- </b>")
			{ 
				input "attr_1", "enum", title: "Attribute 1/8 (This will act as the primary capability for selecting devices):", required: true, multiple: false, options: ATTRIBUTE_TO_SELECTOR, defaultValue: null, submitOnChange: true
				if (attr_1?.size())
				{
					input "attr_2", "string", title: "Attribute 2/8:", required: false, multiple: false, defaultValue: null
					input "attr_3", "string", title: "Attribute 3/8:", required: false, multiple: false, defaultValue: null
					input "attr_4", "string", title: "Attribute 4/8:", required: false, multiple: false, defaultValue: null
					input "attr_5", "string", title: "Attribute 5/8:", required: false, multiple: false, defaultValue: null
					input "attr_6", "string", title: "Attribute 6/8:", required: false, multiple: false, defaultValue: null
					input "attr_7", "string", title: "Attribute 7/8:", required: false, multiple: false, defaultValue: null
					input "attr_8", "string", title: "Attribute 8/8:", required: false, multiple: false, defaultValue: null
				}
			}
			if (attr_1?.size())
			{
				section("<b>-= Save Custom Device Type =- </b>")
				{ 
					href "saveCustomDriverPage", title: "Save", description: "Save this custom device type.",  params: [update: groupName]
					href "saveCustomDriverPage", title: "Delete", description: "Delete this custom device type.", params: [delete: groupName]
				}			
			}		
		}
	}
}


/*
	saveCustomDriverPage
    
	Purpose: Displays the page where shackrat's custom device drivers (SmartPlug, Z-Wave Repeater) are selected to be linked to the controller hub.

	Notes: 	First attempt at organization.
*/
def saveCustomDriverPage(params)
{
	if (params?.delete)
	{
		state?.customDrivers.remove(params.delete)
		app.updateSetting("newDev_AttributeGroup", [type: "string", value: ""])
		app.updateSetting("newDev_DriverName", [type: "string", value: ""])
		app.updateSetting("attr_1", [type: "enum", value: ""])
		7.times
		{
			app.updateSetting("attr_${it+1}", [type: "string", value: ""])
		}
		saveCustomDrivers()
		return customDriverPage()
	}
	
	def deviceClass = params?.update != null ? params?.update : newDev_AttributeGroup

	if (deviceClass.size() && newDev_DriverName?.size() && attr_1?.size())
	{
		def attr = []
		8.times
		{
			if (settings."attr_${it}"?.size())
			{
				attr << settings."attr_${it}"
				app.updateSetting("attr_${it}", [type: "string", value: ""])
			}
		}
		
		def selector = ATTRIBUTE_TO_SELECTOR.find{it.key == attr_1}

		state.customDrivers[deviceClass] = 
		[
			driver: newDev_DriverName,
			selector: selector.value,
			attr: attr
		]

		log.debug state.customDrivers
		saveCustomDrivers()

		app.updateSetting("newDev_AttributeGroup", [type: "string", value: ""])
		app.updateSetting("newDev_DriverName", [type: "string", value: ""])
	}
	else return createCustomDevice()

	log.debug "Saving custom driver map: ${state.customDrivers}"

	dynamicPage(name: "saveCustomDriverPage", uninstall: false, install: false)
	{
		section()
		{
			paragraph title: "Driver Saved!", "The driver definition has been saved."
			href "customDriverPage", title: "Custom Drivers", description: "Manage Custom Drivers"
		}
	}
}


/*
	saveCustomDrivers
    
	Purpose: Saves custom defined drivers to each child app instance.

	Notes: Calls like-named method in child app
*/
def saveCustomDrivers()
{
	childApps.each
	{
	  child ->
		child.saveCustomDrivers(state.customDrivers)
	}
}


/*
	installed
    
	Purpose: Standard install function.

	Notes: Doesn't do much.
*/
def installed()
{
	log.info "${app.name} Installed"

	state.customDrivers = [:]

	initialize()
}


/*
	updated
    
	Purpose: Standard update function.

	Notes: Still doesn't do much.
*/
def updated()
{
	log.info "${app.name} Updated"
	
	if (state?.customDrivers == null)
	{
		state.customDrivers = [:]
	}
	
	unsubscribe()
	initialize()
}


/*
	initialize
    
	Purpose: Initialize the server.

	Notes: Subscribes to the systemStart event to synchronize modes.
*/
def initialize()
{
	log.info "${app.name} Initialized"

	childApps.each
	{
	  child ->
		child.setCommStatus(haltComms)
		log.debug "Found server instance: ${child.label}"
	}

	subscribe(location, "systemStart", systemStartEventHandler)
}


/*
	systemStartEventHandler
    
	Purpose: Event handler for the systemStart event.

	Notes: May do some cleanup in the future, but for now just schedules a mode sync.
*/
def systemStartEventHandler(evt)
{
	app.updateSetting("haltComms",[type: "bool", value: false])
	childApps.each
	{
	  child ->
		child.setCommStatus(false)
		log.debug "System Start: Restoring communications to remote instance: ${child.label}..."
	}
	
	// Push out a mode sync in 20 seconds, after other automations have a chance to determine the correct mode
	runIn(20, "pushSystemMode")
}


/*
	pushSystemMode
    
	Purpose: Pushes the current system mode to all remote hubs.

	Notes: Calls pushCurrentMode() in each server instance.
*/
def pushSystemMode()
{
	log.debug "System Start: Synchronizing modes across all hubs..."
	childApps.each
	{
	  child ->
		child.pushCurrentMode()
	}
}


/*
	aboutPage

	Purpose: Displays the about page with credits.
*/
def aboutPage()
{
	dynamicPage(name: "aboutPage", title: "HubConnect ${currentVersion}", uninstall: false, install: false)
	{
		section()
		{
			paragraph "HubConnect is provided free for personal and non-commercial use.  Countless hours went into the development and testing of this project.  If you like it and would like to see it succeed, or see more apps like this in the future, please consider making a small donation to the cause."
			href "donate", style:"embedded", title: "Please consider making a \$20 or \$40 donation to show your support!", image: "http://irisusers.com/hubitat/hubconnect/donate-icon.png", url: "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=T63P46UYH2TJC&source=url"
		}
		section()
		{
			href "mainPage", title: "Home", description: "Return to HubConnect main menu..."
			paragraph "<span style=\"font-size:.8em\">Remote Client build ${moduleBuild} ${appCopyright}</span>"
		}
	}
}

def getCurrentVersion(){1.1}
def getModuleBuild(){1.4}
def getAppCopyright(){"&copy; 2019 Steve White, Retail Media Concepts LLC <a href=\"https://github.com/shackrat/Hubitat-Private/blob/master/HubConnect/License%20Agreement.md\" target=\"_blank\">HubConnect License Agreement</a>"}
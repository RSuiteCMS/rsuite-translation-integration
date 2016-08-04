rsuite-translation-integration
-----

THIS IS AN IN-PROGRESS VERSION OF A PLUGIN DEVELOPED FOR DEMONSTRATIONS

This plugin provides methods to send/retrieve files to/from other systems for language translation. 

To use this plugin, you must add menu actions to your own plugin that reference services in this plugin.

To use:

1) Add this jar as a dependency for your plugin project.

2) Adjust your build process to deploy the plugin to your RSuite installation.

3) In your own plugin, configure the language datatype values you want to use e.g.,:
	<datatypeDefinition name="languageDt">
		<optionList>
			<option label="English - US" value="en-US"/>
			<option label="Chinese Simplified" value="zh-CN"/>
			<option label="Chinese Traditional" value="zh"/>
			<option label="Danish" value="da"/>
			...
		</optionList>
	</datatypeDefinition>

4) Add the actions you want, similar to these: 

	<contextMenuRuleSet name="menu.rsuite:translationsToBriefcase">
		<menuItemList>
			<menuItem id="menu.rsuite.translationsToBriefcase">
				<actionName>rsuite:invokeWebservice</actionName>
				<label>Put translations on Briefcase</label>
				<property name="remoteApiName" value="translation.ws.AddTranslationsToBriefcase"/>
				<property name="rsuite:group" value="translation"/>
				<property name="rsuite:path" value="Translation"/>
			</menuItem>
		</menuItemList>
		<ruleList>
			<rule>include nodeType mo</rule>
		</ruleList>
	</contextMenuRuleSet>
	<contextMenuRuleSet name="menu.rsuite:translationsRequest">
		<menuItemList>
			<menuItem id="menu.rsuite.translationsRequest">
				<actionName>rsuite:invokeWebservice</actionName>
				<label>Request translations (XTM)</label>
				<property name="remoteApiName" value="translation.ws.RequestTranslations"/>
				<property name="formId" value="form.translation.translationRequest"/>
				<property name="rsuite:group" value="translation"/>
				<property name="rsuite:path" value="Translation"/>
				<property name="serviceParams.client" value="IIC"/>
			</menuItem>
		</menuItemList>
		<ruleList>
			<rule>include nodeType mo</rule>
		</ruleList>
	</contextMenuRuleSet>

5) Add translation columns to content results, if you want them:

	<searchResultsConfiguration>
		<columnList>
			...
			<column name="language" label="Lang"/>
			<column name="translationOf" label="Translation Of"/>
			<column name="translationStatus" label="Status"/>
			...
		</columnList>
	</searchResultsConfiguration>

6) In the Admin Console, delete the scheduled jobs you don't want to use.
	

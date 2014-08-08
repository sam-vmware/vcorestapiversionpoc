package com.vmware.vcopoc.conf

/**
 * author: samueldoyle
 * holds various configuration values
 * To override these set the bindparams in the validate method prior to WorkFlowUtils.instance.reinitConfig(
 * See VcoGenericPatchingFlow.validate for an example.
 * Anything without a defaultValue will fail validation if the value isn't defined. At the moment the minimum required
 * is machineName, machineClass, platform and patchLocation
 */

vco {
    workflow {
        names {
            manageMachineBasic = 'Manage Machine Basic'
            appDPatching = 'AppD Patching'
            assessCompliance = 'Assess Patch Compliance'
            findMachineIdByName {
                wfName = 'Find Machine Id by Name'
                inAttributes = [
                    [name: 'machineName', type: 'string', value: machineName]
                ]
            }
            genericPatching {
                wfName = 'Generic Machine Patching'
                inAttributes = [
                     [name: 'machineName', type: 'string', value: machineName],
                     [name: 'machineClass', type: 'string', value: machineClass],
                     [name: 'platform', type: 'string', value: platform],
                     [name: 'definePatchLocation', type: 'boolean', value: definePatchLocation, defaultValue: true],
                     [name: 'patchLocation', type: 'string', value: patchLocation],
                     [name: 'skipReboots', type: 'boolean', value: skipReboots, defaultValue: true],
                     [name: 'endOfRunOnlyReboot', type: 'boolean', value: endOfRunOnlyReboot, defaultValue: false],
                     [name: 'MachineDomainName', type: 'string', value: machineDomainName, defaultValue: 'local'],
                     [name: 'domainType', type: 'string', value: domainType, defaultValue: 'DNS'],
                     [name: 'port', type: 'string', value: port, defaultValue: '26542'],
                     [name: 'protocol', type: 'string', value: protocol, defaultValue: 'HTTP'],
                     [name: 'isHttpProxy', type: 'string', value: isHttpProxy, defaultValue: 'No'],
                     [name: 'bStaticConnectionString', type: 'boolean', value: useStaticConnectionString, defaultValue: false],
                     [name: 'resetMutualAuthentication', type: 'boolean', value: resetMutualAuthentication, defaultValue: false],
                     [name: 'bSendEmail', type: 'boolean', value: sendEmail, defaultValue: false],
                     [name: 'emailToAddress', type: 'string', value: emailToAddress, defaultValue: ''],
                     [name: 'maxDeploymentJobs', type: 'number', value: maxDeploymentJobs, defaultValue: 99],
                     [name: 'maxRootBulletinsToDeploy', type: 'number', value: maxRootBulletinsToDeploy, defaultValue: 30],
                     [name: 'defineAdvancedSettings', type: 'boolean', value: defineAdvancedSettings, defaultValue: true],
                     [name: 'onlyAssess', type: 'boolean', value: onlyAssess, defaultValue: false],
                     [name: 'defineExceptionArray', type: 'boolean', value: defineExceptionArray, defaultValue: false],
                     [name: 'exceptionsArrayString', type: 'string', value: exceptionsArrayString, defaultValue: ''],
                     [name: 'defineIncludeArray', type: 'boolean', value: defineIncludeArray, defaultValue: false],
                     [name: 'includeBulletinsArrayString', type: 'string', value: includeBulletinsArrayString, defaultValue: ''],
                     [name: 'defineCommandLineSwitches', type: 'boolean', value: defineCommandLineSwitches, defaultValue: false],
                     [name: 'commandLineSwitches', type: 'string', value: commandLineSwitches, defaultValue: ''],
                     [name: 'defineKillMinutes', type: 'boolean', value: defineKillMinutes, defaultValue: false],
                     [name: 'killMinutes', type: 'string', value: killMinutes, defaultValue: ''],
                     [name: 'defineRunLevel', type: 'boolean', value: defineRunLevel, defaultValue: false],
                     [name: 'runLevel', type: 'string', value: runLevel, defaultValue: ''],
                     [name: 'runPreRemoteCommand', type: 'boolean', value: runPreRemoteCommand, defaultValue: false],
                     [name: 'preRemoteCommandName', type: 'string', value: preRemoteCommandName, defaultValue: ''],
                     [name: 'runPostRemoteCommand', type: 'boolean', value: runPostRemoteCommand, defaultValue: false],
                     [name: 'postRemoteCommandName', type: 'string', value: postRemoteCommandName, defaultValue: '']
                ]
            }
        }
        status {
            complete = 'complete'
            completed = 'completed'
            running = 'running'
        }
    }
    webservice {
        methods {
            executeWorkFlow = 'executeWorkflow'
            getWorkflowsWithName = 'getWorkflowsWithName'
            getWorkflowTokenResult = 'getWorkflowTokenResult'
            getWorkflowTokenStatus = 'getWorkflowTokenStatus'
        }
        types {
            tokenAttribute = 'ch.dunes.vso.webservice.WorkflowTokenAttribute'
        }
    }
}

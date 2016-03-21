/*
 * #%L
 * LearnPAd Simulator
 * %%
 * Copyright (C) 2014 - 2015 Linagora
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
'use strict';

function processReceiver(address) {

    var newProcessReceiver = {};

    newProcessReceiver.processFinishOnError = false;
    newProcessReceiver.processData = {};

    var location = 'ws://' + address + '/process';

    newProcessReceiver._onopen = function() {
        // nothing to do
    };

    newProcessReceiver._onmessage = function(m) {
        var msg = JSON.parse(m.data);
        switch (msg.type) {
        case 'DATA':
            newProcessReceiver.processData = msg;
            newProcessReceiver.updateProcessList(msg);
            break;
        }
    };

    newProcessReceiver._onclose = function(m) {
        $('#formPosition').html('');
        $('#processTable').html('');
        $('#processInfo').html('Lost connection with server');

        newProcessReceiver.ws = null;
        if (newProcessReceiver.processFinishOnError) {
            alert('The following error occurred: ' +
                  m.reason +
                  ' (' + m.code + ')');
        }
    };

    newProcessReceiver._onerror = function(e) {
        newProcessReceiver.processFinishOnError = true;
    };

    newProcessReceiver.send = function(data) {
        newProcessReceiver.ws.send(data);
    };

    newProcessReceiver.instanciate = function(processId) {
        var process = newProcessReceiver.processData.processes.filter(
            function(e) {
                return e.id == processId;
            })[0];

        processFormGenerate(process,
                            '#formPosition',
                            function(values) {
                                newProcessReceiver.submitProcessData(
                                    processId,
                                    values
                                );
                                $('#formPosition').html('');
                                $('#processInfo').html('Process ' +
                                                       process.name +
                                                       ' instanciated');
                            });
    };

    newProcessReceiver.submitProcessData = function(id, values) {

        // SPECIAL MODIFICATIONS FOR DEMO PURPOSE ONLY
        if(id == 'titulounico') {

            // un-stringify
            values = JSON.parse(values);

            if(values['case'] === '829-2015') {
                values['applicantName'] = 'Guiseppe Cappelletti';
                values['applicationCity'] = 'lpd:Ancona';
                values['applicationZone'] = 'lpd:Beach_Area_At_The_Sea';
                values['applicationPublicAdministration'] = 'ldp:SUAPSenigallia';
                values['applicationSubType'] = 'ldp:Restructuring';
                values['applicationSector'] = 'lpd:Building_Sector';
                values['applicationBusinessActivity'] = 'lpd:Receptive_Tourism_Activity';
                values['applicationDescription'] = 'Realization of a chalet on a beach area of Senigallia';
                values['applicationATECOCategory'] = '';
            } else {
                values['applicantName'] = 'Ottavio Nandi';
                values['applicationCity'] = 'lpd:Belforte_del_Chienti';
                values['applicationZone'] = 'lpd:Regional_Protected_Area_Unione_Montana_Monti_Azzurri';
                values['applicationPublicAdministration'] = 'lpd:SUAPMontiAzzurri';
                values['applicationSubType'] = 'lpd:Reactivation';
                values['applicationSector'] = 'lpd:Waste_Sector';
                values['applicationBusinessActivity'] = 'lpd:Industrial';
                values['applicationDescription'] = 'request for reneval of authorization of industrial waste water discharge in sewer - coffee machines factory';
                values['applicationATECOCategory'] = '28.93';
            }

            // re-stringify
            values = JSON.stringify(values);
        }

        newProcessReceiver.send(JSON.stringify(
            {
                type: 'INSTANCIATE',
                id: id,
                parameters: values
            }
        ));
    };

    newProcessReceiver.updateProcessList = function(msg) {
        for (var i = 0; i < msg.processes.length; i++) {
            var process = msg.processes[i];
            var content = '';
            content += '<tr id=\'process' + process.id + '\'>';
            content += '<td>' + process.name + '</td>';
            content += '<td>' + process.description + '</td>';
            content += '</tr>';
            $('#processTable tbody').append(content);

            // add event listener
            // (due to the way closures work in js (ie they dont)
            // we need to create a function which will be called
            // each time and explicitely pass the process)
            document.getElementById('process' + process.id).
                onclick = function(p) {
                    return function() {
                        newProcessReceiver.instanciate(p.id);
                    };
                }(process);
        }
    };

    newProcessReceiver.ws = new WebSocket(location);
    newProcessReceiver.ws.onopen = newProcessReceiver._onopen;
    newProcessReceiver.ws.onmessage = newProcessReceiver._onmessage;
    newProcessReceiver.ws.onclose = newProcessReceiver._onclose;
    newProcessReceiver.ws.onerror = newProcessReceiver._onerror;

    return newProcessReceiver;
 }

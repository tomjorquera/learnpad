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

function formaas_instance(formServer) {

    var newFormaas = {};

    newFormaas.poller = function poller() {
        var newPoller = {};

        newPoller.timeoutId = -1;

        newPoller.start = function(onsuccess) {
            var timeout_func = function() {
                $.ajax({ url: formServer, success: onsuccess }, 1000);
                newPoller.timeoutId = setTimeout(timeout_func, 3000);
            };

            newPoller.timeoutId = setTimeout(timeout_func, 300);
        };

        newPoller.stop = function() {
            clearTimeout(newPoller.timeoutId);
        };

        return newPoller;
    };

    newFormaas.sendREST = function(type, url, body) {
        var client = new XMLHttpRequest();
        client.open(type, url, false);
        if (typeof body !== 'undefined') {
            client.setRequestHeader('Content-Type', 'application/json');
            client.send(JSON.stringify(body));
        } else {
            client.send();
        }
        return client.responseText;
    };

    newFormaas.createForm = function(formName, desc, model) {
        // create form model
        var form = {
            name: formName,
            description: desc,
            model: JSON.parse(model)
        };
        var formId = JSON.parse(
            newFormaas.sendREST('POST', formServer + '/forms', form)
        )._id;

        // instanciate form
        var instance = {
            name: formName,
            description: desc,
            form: formId
        }

        var instanceId = JSON.parse(
            newFormaas.sendREST('POST', formServer + '/instances', instance)
        )._id;

        return { formId: formId, instanceId: instanceId }
    };

    newFormaas.deleteForm = function(form) {
        // delete instance
        newFormaas.sendREST('DELETE', formServer + '/instances/' +
                            form.instanceId);
        //delete form model
        newFormaas.sendREST('DELETE', formServer + '/forms/' + form.formId);
    };

    newFormaas.getFormResult = function(formId) {
        return newFormaas.sendREST('GET', formServer +
                                        '/results?instance=' + formId, '');
    };

    return newFormaas;
}

var formServer = 'http://localhost:3000';
var formaas = formaas_instance(formServer);

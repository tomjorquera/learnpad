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

function taskFormGenerate(taskid, data, formContainer, formId, callback) {

    var newGenerator = {};

    newGenerator.model = data.form;

    newGenerator.form = formaas.createForm(
        'Task ' + taskid,
        data.description,
        newGenerator.model
    );

    newGenerator.poller = formaas.poller(formServer);
    newGenerator.poller.start(function() {
        var res = formaas.getFormResult(newGenerator.form.instanceId);
        if (res != '[]') {
            newGenerator.poller.stop();
            formaas.deleteForm(newGenerator.form);
            callback(JSON.stringify(res));
        }
    });

    $('#' + formContainer).addClass(
        'embed-responsive embed-responsive-4by3');
    $('#' + formContainer).html(
        '<iframe class="embed-responsive-item" src="' + formServer +
            '/apps/form/' + newGenerator.form.instanceId +
            '"></iframe>'
    );

    return newGenerator;
}

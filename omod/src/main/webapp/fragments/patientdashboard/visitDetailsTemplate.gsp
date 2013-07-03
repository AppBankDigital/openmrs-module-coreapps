<div class="status-container">
    [[ if (stopDatetime) { ]]
        <i class="icon-time small"></i> ${ ui.message("emr.visitDetails", '[[- startDatetime ]]', '[[- stopDatetime ]]') }
    [[ } else { ]]
        <span class="status active"></span> ${ ui.message("emr.activeVisit") }
        <i class="icon-time small"></i>
        ${ ui.message("emr.activeVisit.time", '[[- startDatetime ]]') }
    [[ } ]]
    <% if (featureToggles.isFeatureEnabled("editVisitDates") && !emrContext.activeVisit) { %>
        <a class="right" href="#" data-visit-id="[[= id]]">${ ui.message("coreapps.task.editVisitDate.label") }</a>
    <% } %>
</div>

<div class="visit-actions [[- stopDatetime ? 'past-visit' : 'active-visit' ]]">
    [[ if (stopDatetime) { ]]
        <p class="label"><i class="icon-warning-sign small"></i> ${ ui.message("coreapps.patientDashboard.actionsForInactiveVisit") }</p>
    [[ } ]]
    <% visitActions.each { task ->
        def url = task.url
        if (task.type != "script") {
            url = "/" + contextPath + "/" + url
    %>
        <% if (task.require) { %>
            [[ if ((function() { var patientId = ${ patient.id }; var visit = { id: id, active: stopDatetime == null }; return (${ task.require }); })()) { ]]
        <% } %>
            <a href="[[= emr.applyContextModel('${ ui.escapeJs(url) }', { patientId: ${ patient.id }, 'visit.id': id, 'visit.active': stopDatetime == null }) ]]" class="button task">
        <% } else { // script
            url = "javascript:" + task.script
        %>
            <a href="${ url }" class="button task">
        <% } %>
            <i class="${task.icon}"></i> ${ ui.message(task.label) }
            </a>
        <% if (task.require) { %>
            [[ } ]]
        <% } %>
    <% } %>
</div>

<h4>${ ui.message("emr.patientDashBoard.encounters")} </h4>
<ul id="encountersList">
    [[ _.each(encounters, function(encounter) { ]]
        [[ if (!encounter.voided) { ]]
            [[= encounterTemplates.displayEncounter(encounter, patient) ]]
        [[  } ]]
    [[ }); ]]
</ul>
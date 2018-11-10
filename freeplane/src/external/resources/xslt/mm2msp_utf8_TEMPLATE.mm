<map version="0.9.0">
<!-- To view this file, download free mind mapping software Freeplane from https://www.freeplane.org -->
<node CREATED="1216809870908" ID="ID_1507004962" MODIFIED="1216826585940" TEXT="Example of project plan ready for export&#xa;using XSLT export with mm2msp_utf8.xsl">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      The note of the root node is exported as project comments (in the properties) but <b>formatting</b> is <i>lost. </i>
    </p>
    <p>
      Any attribute starting with the prefix &quot;prj-&quot; is used as parameter for the project (after removal of the prefix). Possible parameter/value combinations need to be guessed from the XML output of MS Project. The below lines give some examples and hints.
    </p>
    <p>
      Any attribute starting with the prefix &quot;tsk-&quot; is used as parameter for the task (after removal of the prefix)
    </p>
    <h2>
      Examples of possible project parameters and values
    </h2>
    <p>
      The following lines show examples of parameters/values combinations found in MS Project's XML file. Combinations in Bold are easy to understand, meaningful and safe to use, ones in Italic are to be avoided, for the others let me know.
    </p>
    <div class="e">
      <div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &lt;Name&gt;Project1.xml&lt;/Name&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; <i>&lt;Title&gt;Project Name Bla Bla&lt;/Title&gt; DO NOT USE, generated from the node text.</i>
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; <b>&lt;Company&gt;Acme Corp.&lt;/Company&gt;</b>
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; <b>&lt;Author&gt;John Smith&lt;/Author&gt;</b>
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;CreationDate&gt;2008-07-23T12:48:00&lt;/CreationDate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;LastSaved&gt;2008-07-23T16:37:00&lt;/LastSaved&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;ScheduleFromStart&gt;1&lt;/ScheduleFromStart&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;StartDate&gt;2008-07-23T08:00:00&lt;/StartDate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;FinishDate&gt;2008-07-29T17:00:00&lt;/FinishDate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;FYStartDate&gt;1&lt;/FYStartDate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;CriticalSlackLimit&gt;0&lt;/CriticalSlackLimit&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;CurrencyDigits&gt;2&lt;/CurrencyDigits&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;CurrencySymbol&gt;&#8364;&lt;/CurrencySymbol&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;CurrencySymbolPosition&gt;0&lt;/CurrencySymbolPosition&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;CalendarUID&gt;1&lt;/CalendarUID&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DefaultStartTime&gt;08:00:00&lt;/DefaultStartTime&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DefaultFinishTime&gt;17:00:00&lt;/DefaultFinishTime&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;MinutesPerDay&gt;480&lt;/MinutesPerDay&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;MinutesPerWeek&gt;2400&lt;/MinutesPerWeek&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DaysPerMonth&gt;20&lt;/DaysPerMonth&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DefaultTaskType&gt;0&lt;/DefaultTaskType&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DefaultFixedCostAccrual&gt;3&lt;/DefaultFixedCostAccrual&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DefaultStandardRate&gt;0&lt;/DefaultStandardRate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DefaultOvertimeRate&gt;0&lt;/DefaultOvertimeRate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DurationFormat&gt;7&lt;/DurationFormat&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;WorkFormat&gt;2&lt;/WorkFormat&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;EditableActualCosts&gt;0&lt;/EditableActualCosts&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;HonorConstraints&gt;0&lt;/HonorConstraints&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;InsertedProjectsLikeSummary&gt;1&lt;/InsertedProjectsLikeSummary&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;MultipleCriticalPaths&gt;0&lt;/MultipleCriticalPaths&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;NewTasksEffortDriven&gt;1&lt;/NewTasksEffortDriven&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;NewTasksEstimated&gt;1&lt;/NewTasksEstimated&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;SplitsInProgressTasks&gt;1&lt;/SplitsInProgressTasks&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;SpreadActualCost&gt;0&lt;/SpreadActualCost&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;SpreadPercentComplete&gt;0&lt;/SpreadPercentComplete&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;TaskUpdatesResource&gt;1&lt;/TaskUpdatesResource&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;FiscalYearStart&gt;0&lt;/FiscalYearStart&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;WeekStartDay&gt;1&lt;/WeekStartDay&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;MoveCompletedEndsBack&gt;0&lt;/MoveCompletedEndsBack&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;MoveRemainingStartsBack&gt;0&lt;/MoveRemainingStartsBack&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;MoveRemainingStartsForward&gt;0&lt;/MoveRemainingStartsForward&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;MoveCompletedEndsForward&gt;0&lt;/MoveCompletedEndsForward&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;BaselineForEarnedValue&gt;0&lt;/BaselineForEarnedValue&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;AutoAddNewResourcesAndTasks&gt;1&lt;/AutoAddNewResourcesAndTasks&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;CurrentDate&gt;2008-07-23T08:00:00&lt;/CurrentDate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;MicrosoftProjectServerURL&gt;1&lt;/MicrosoftProjectServerURL&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;Autolink&gt;1&lt;/Autolink&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;NewTaskStartDate&gt;0&lt;/NewTaskStartDate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;DefaultTaskEVMethod&gt;0&lt;/DefaultTaskEVMethod&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;ProjectExternallyEdited&gt;0&lt;/ProjectExternallyEdited&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;ExtendedCreationDate&gt;1984-01-01T00:00:00&lt;/ExtendedCreationDate&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;ActualsInSync&gt;1&lt;/ActualsInSync&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;RemoveFileProperties&gt;0&lt;/RemoveFileProperties&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;AdminProject&gt;0&lt;/AdminProject&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;OutlineCodes /&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;WBSMasks /&gt;
          </div>
        </div>
        <div class="e">
          <div style="margin-left: 0; text-indent: 0">
            &#160; &lt;ExtendedAttributes /&gt;
          </div>
        </div>
      </div>
    </div>
  </body>
</html></richcontent>
<attribute NAME="prj-Company" VALUE="Acme Corp."/>
<attribute NAME="prj-Author" VALUE="John Smith"/>
<node CREATED="1216809884057" ID="ID_596157601" MODIFIED="1216825866317" POSITION="right" TEXT="task 1">
<node CREATED="1216809884057" ID="ID_551098884" MODIFIED="1216826500604" TEXT="task 1.1">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Any attribute starting with the prefix &quot;tsk-&quot; is used as parameter for the task (after removal of the prefix). Possible parameter/value combinations need to be guessed from the XML output of MS Project. The below lines give some examples and hints.
    </p>
    <h2>
      Examples of possible tasks parameters and values
    </h2>
    <p>
      The following lines show examples of parameters/values combinations found in MS Project's XML file. Combinations in Bold are easy to understand, meaningful and safe to use, ones in Italic are to be avoided, for the others let me know.
    </p>
    <div class="e">
      <div>
        <div class="e">
          <div>
            <div class="e">
              <div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; <i>&lt;UID&gt;1&lt;/UID&gt; DO NOT USE, generated automatically.</i>
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ID&gt;1&lt;/ID&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    <i>&#160; &lt;Name&gt;task 1&lt;/Name&gt; DO NOT USE, generated from the node text.</i>
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Type&gt;1&lt;/Type&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;IsNull&gt;0&lt;/IsNull&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;CreateDate&gt;2008-07-23T12:48:00&lt;/CreateDate&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;WBS&gt;1&lt;/WBS&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;OutlineNumber&gt;1&lt;/OutlineNumber&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;OutlineLevel&gt;1&lt;/OutlineLevel&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; <b>&lt;Priority&gt;200&lt;/Priority&gt;</b>
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Start&gt;2008-07-22T08:00:00&lt;/Start&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Finish&gt;2008-07-29T17:00:00&lt;/Finish&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Duration&gt;PT48H0M0S&lt;/Duration&gt; the duration of the task in hours/minutes/seconds (but doesn't work as such).
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;DurationFormat&gt;53&lt;/DurationFormat&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Work&gt;PT8H0M0S&lt;/Work&gt; the effort required for the task in hours/minutes/seconds (but doesn't work as such).
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ResumeValid&gt;0&lt;/ResumeValid&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;EffortDriven&gt;0&lt;/EffortDriven&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Recurring&gt;0&lt;/Recurring&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;OverAllocated&gt;0&lt;/OverAllocated&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Estimated&gt;1&lt;/Estimated&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Milestone&gt;0&lt;/Milestone&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Summary&gt;1&lt;/Summary&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Critical&gt;1&lt;/Critical&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;IsSubproject&gt;0&lt;/IsSubproject&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;IsSubprojectReadOnly&gt;0&lt;/IsSubprojectReadOnly&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ExternalTask&gt;0&lt;/ExternalTask&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;EarlyStart&gt;2008-07-22T08:00:00&lt;/EarlyStart&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;EarlyFinish&gt;2008-07-29T17:00:00&lt;/EarlyFinish&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;LateStart&gt;2008-07-23T08:00:00&lt;/LateStart&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;LateFinish&gt;2008-07-29T17:00:00&lt;/LateFinish&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;StartVariance&gt;0&lt;/StartVariance&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;FinishVariance&gt;0&lt;/FinishVariance&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;WorkVariance&gt;0&lt;/WorkVariance&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;FreeSlack&gt;0&lt;/FreeSlack&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;TotalSlack&gt;0&lt;/TotalSlack&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;FixedCost&gt;0&lt;/FixedCost&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;FixedCostAccrual&gt;1&lt;/FixedCostAccrual&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;PercentComplete&gt;0&lt;/PercentComplete&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;PercentWorkComplete&gt;0&lt;/PercentWorkComplete&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Cost&gt;0&lt;/Cost&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;OvertimeCost&gt;0&lt;/OvertimeCost&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;OvertimeWork&gt;PT0H0M0S&lt;/OvertimeWork&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ActualDuration&gt;PT0H0M0S&lt;/ActualDuration&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ActualCost&gt;0&lt;/ActualCost&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ActualOvertimeCost&gt;0&lt;/ActualOvertimeCost&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ActualWork&gt;PT0H0M0S&lt;/ActualWork&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ActualOvertimeWork&gt;PT0H0M0S&lt;/ActualOvertimeWork&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;RegularWork&gt;PT0H0M0S&lt;/RegularWork&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;RemainingDuration&gt;PT48H0M0S&lt;/RemainingDuration&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;RemainingCost&gt;0&lt;/RemainingCost&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;RemainingWork&gt;PT0H0M0S&lt;/RemainingWork&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;RemainingOvertimeCost&gt;0&lt;/RemainingOvertimeCost&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;RemainingOvertimeWork&gt;PT0H0M0S&lt;/RemainingOvertimeWork&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ACWP&gt;0&lt;/ACWP&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;CV&gt;0&lt;/CV&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ConstraintType&gt;0&lt;/ConstraintType&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;CalendarUID&gt;-1&lt;/CalendarUID&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;LevelAssignments&gt;1&lt;/LevelAssignments&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;LevelingCanSplit&gt;1&lt;/LevelingCanSplit&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;LevelingDelay&gt;0&lt;/LevelingDelay&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;LevelingDelayFormat&gt;8&lt;/LevelingDelayFormat&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;IgnoreResourceCalendar&gt;0&lt;/IgnoreResourceCalendar&gt;
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="e">
      <div>
        <div class="e">
          <div>
            <div class="e">
              <div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    <i>&#160; &lt;Notes&gt;bla bla&lt;/Notes&gt; DO NOT USE, will be generated from the node's note.</i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="e">
      <div>
        <div class="e">
          <div>
            <div class="e">
              <div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;HideBar&gt;0&lt;/HideBar&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;Rollup&gt;0&lt;/Rollup&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;BCWS&gt;0&lt;/BCWS&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;BCWP&gt;0&lt;/BCWP&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;PhysicalPercentComplete&gt;0&lt;/PhysicalPercentComplete&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;EarnedValueMethod&gt;0&lt;/EarnedValueMethod&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ActualWorkProtected&gt;PT0H0M0S&lt;/ActualWorkProtected&gt;
                  </div>
                </div>
                <div class="e">
                  <div style="text-indent: 0; margin-left: 0">
                    &#160; &lt;ActualOvertimeWorkProtected&gt;PT0H0M0S&lt;/ActualOvertimeWorkProtected&gt;
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </body>
</html>
</richcontent>
<attribute_layout NAME_WIDTH="107" VALUE_WIDTH="107"/>
<attribute NAME="tsk-Priority" VALUE="200"/>
</node>
<node CREATED="1216809890907" ID="ID_1321935745" MODIFIED="1216825825567" TEXT="task 1.2">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      The note of any node is exported as task notes but <b>formatting</b> is <i>lost. </i>
    </p>
    <p>
      The sub-nodes show how graphical links are transformed into predecessors relations.
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1216809898648" ID="ID_896868759" MODIFIED="1216825200032" TEXT="task 1.2.1 - predecessor of all following tasks">
<arrowlink DESTINATION="ID_661978653" ENDARROW="Default" ENDINCLINATION="199;0;" ID="Arrow_ID_1192464141" STARTARROW="Default" STARTINCLINATION="199;0;"/>
<arrowlink DESTINATION="ID_1453547974" ENDARROW="None" ENDINCLINATION="76;0;" ID="Arrow_ID_831124590" STARTARROW="Default" STARTINCLINATION="76;0;"/>
<arrowlink DESTINATION="ID_869034720" ENDARROW="None" ENDINCLINATION="94;0;" ID="Arrow_ID_851487381" STARTARROW="None" STARTINCLINATION="94;0;"/>
<arrowlink DESTINATION="ID_1048991478" ENDARROW="Default" ENDINCLINATION="195;0;" ID="Arrow_ID_1795574541" STARTARROW="None" STARTINCLINATION="195;0;"/>
</node>
<node CREATED="1216809902163" ID="ID_1048991478" MODIFIED="1216825125382" TEXT="task 1.2.2 - uses Finish-to-Start relation"/>
<node CREATED="1216809906259" ID="ID_661978653" MODIFIED="1216825194484" TEXT="task 1.2.3 - uses Start-to-Start relation"/>
<node CREATED="1216825049830" ID="ID_1453547974" MODIFIED="1216825170078" TEXT="task 1.2.4 - uses Start-to-Finish relation"/>
<node CREATED="1216825055158" ID="ID_869034720" MODIFIED="1216825200032" TEXT="task 1.2.5 - uses Finish-to-Finish relation"/>
</node>
</node>
<node CREATED="1216809914482" ID="ID_1308741003" MODIFIED="1216826803952" POSITION="left" TEXT="task 2 - how to export a mindmap to MS Project ?">
<node CREATED="1216809917636" ID="ID_199484608" MODIFIED="1216826829891" TEXT="task 2.1 - create a map following the notes and hints expressed in this example map"/>
<node CREATED="1216809921221" ID="ID_1681718272" MODIFIED="1216826859865" TEXT="task 2.2 - export the map using the File -&gt; Export -&gt; Using XSLT... menu">
<node CREATED="1216826868748" ID="ID_1660904657" MODIFIED="1216826921937" TEXT="task 2.2.1 - select the mm2msp_utf8.xsl XSL file from the accessories directory in the Freeplane base directory."/>
<node CREATED="1216826924521" ID="ID_1561412985" MODIFIED="1216827030567" TEXT="task 2.2.2 - export to a file with a name ending in .xml"/>
</node>
<node CREATED="1216826940554" ID="ID_769680777" MODIFIED="1216827227358" TEXT="task 2.3 - open Microsoft Office Project">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      You need a version of MS Project supporting XML, I think MS Project 2003 and later.
    </p>
  </body>
</html>
</richcontent>
</node>
<node CREATED="1216826953904" ID="ID_999549737" MODIFIED="1216826968235" TEXT="task 2.4 - select the File -&gt; Open menu point">
<node CREATED="1216826972942" ID="ID_1853962830" MODIFIED="1216827047893" TEXT="task 2.4.1 - make sure the file dialog filters on XML files"/>
<node CREATED="1216827051148" ID="ID_1363816861" MODIFIED="1216827068093" TEXT="task 2.4.2 - select the file.xml you&apos;ve just exported"/>
</node>
<node CREATED="1216827072099" ID="ID_785390572" MODIFIED="1216827091417" TEXT="task 2.5 - you&apos;re done, enjoy!"/>
</node>
<node CREATED="1216809929423" ID="ID_180931108" MODIFIED="1216825071502" POSITION="left" TEXT="task 3"/>
<node CREATED="1216822804682" ID="ID_1397543137" MODIFIED="1216825071502" POSITION="left" TEXT="task 4"/>
</node>
</map>

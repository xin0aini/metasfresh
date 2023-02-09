@from:cucumber
@dev:runThisOne
Feature: Test logs

  Scenario:
    Given infrastructure and metasfresh are running
    And the existing user with login 'metasfresh' receives a random a API token for the existing role with name 'WebUI'
    And log workpackage

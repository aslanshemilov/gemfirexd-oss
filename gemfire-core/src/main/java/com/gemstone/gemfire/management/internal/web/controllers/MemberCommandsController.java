/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.management.internal.web.controllers;

import com.gemstone.gemfire.management.internal.cli.i18n.CliStrings;
import com.gemstone.gemfire.management.internal.cli.util.CommandStringBuilder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The MemberCommandsController class implements GemFire Management REST API web service endpoints for the
 * Gfsh Member Commands.
 * <p/>
 * @author John Blum
 * @see com.gemstone.gemfire.management.internal.cli.commands.MemberCommands
 * @see com.gemstone.gemfire.management.internal.web.controllers.AbstractCommandsController
 * @see org.springframework.stereotype.Controller
 * @see org.springframework.web.bind.annotation.PathVariable
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @see org.springframework.web.bind.annotation.RequestMethod
 * @see org.springframework.web.bind.annotation.RequestParam
 * @see org.springframework.web.bind.annotation.ResponseBody
 * @since 7.5
 */
@Controller("memberController")
@RequestMapping("/v1")
@SuppressWarnings("unused")
public class MemberCommandsController extends AbstractCommandsController {

  @RequestMapping(value="/members", method=RequestMethod.GET)
  @ResponseBody
  //public String listMembers(@RequestBody MultiValueMap<String, String> requestParameters) {
  //public String listMembers(@RequestParam(value = "group", required = false) final String groupName,
  //                          @RequestParam(value = "group", required = false) final String[] groupNames) {
  public String listMembers(@RequestParam(value = "group", required = false) final String groupName) {
    final CommandStringBuilder command = new CommandStringBuilder(CliStrings.LIST_MEMBER);

    //logger.info(String.format("Request Body: %1$s", requestParameters));
    //logger.info(String.format("Request Parameter (group): %1$s", groupName));
    //logger.info(String.format("Request Parameter (group) as array: %1$s", ArrayUtils.toString(groupNames)));

    //final String groupName = requestParameters.getFirst("group");

    if (hasValue(groupName)) {
      command.addOption(CliStrings.LIST_MEMBER__GROUP, groupName);
    }

    return processCommand(command.toString());
  }

  @RequestMapping(value="/members/{name}", method=RequestMethod.GET)
  @ResponseBody
  public String describeMember(@PathVariable("name") final String memberNameId) {
    final CommandStringBuilder command = new CommandStringBuilder(CliStrings.DESCRIBE_MEMBER);
    command.addOption(CliStrings.DESCRIBE_MEMBER__IDENTIFIER, memberNameId);
    return processCommand(command.toString());
  }

}
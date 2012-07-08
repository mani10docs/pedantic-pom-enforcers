/*
 * Copyright (c) 2012 by The Author(s)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ferstl.maven.pomenforcers;

public interface PedanticEnforcerVisitor {

  void visit(PedanticPomSectionOrderEnforcer sectionOrderEnforcer);
  void visit(PedanticModuleOrderEnforcer moduleOrderEnforcer);
  void visit(PedanticDependencyManagementOrderEnforcer dependencyManagementOrderEnforcer);
  void visit(PedanticDependencyOrderEnforcer dependencyOrderEnforcer);
  void visit(PedanticDependencyConfigurationEnforcer pedanticDependencyConfigurationEnforcer);
  void visit(PedanticPluginManagementOrderEnforcer pluginManagementOrderEnforcer);
  void visit(CompoundPedanticEnforcer compoundEnforcer);
  void visit(PedanticPluginConfigurationEnforcer pedanticPluginConfigurationEnforcer);
  void visit(PedanticPluginManagementLocationEnforcer pedanticPluginManagementLocationEnforcer);
}
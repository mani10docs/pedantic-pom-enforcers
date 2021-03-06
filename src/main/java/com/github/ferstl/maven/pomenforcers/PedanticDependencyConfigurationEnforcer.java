/*
 * Copyright (c) 2012 - 2019 the original author or authors.
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

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.github.ferstl.maven.pomenforcers.model.DependencyModel;
import static com.github.ferstl.maven.pomenforcers.ErrorReport.toList;

/**
 * This enforcer makes sure that dependency versions and exclusions are declared in the
 * <code>&lt;dependencyManagement&gt;</code> section.
 * <pre>
 * ### Example
 *     &lt;rules&gt;
 *       &lt;dependencConfiguration implementation=&quot;com.github.ferstl.maven.pomenforcers.PedanticDependencyConfigurationEnforcer&quot;&gt;
 *         &lt;!-- Manage dependency versions in dependency management --&gt;
 *         &lt;manageVersions&gt;true&lt;/manageVersions&gt;
 *         &lt;!-- allow ${project.version} outside dependency management --&gt;
 *         &lt;allowUnmanagedProjectVersions&gt;true&lt;/allowUnmanagedProjectVersions&gt;
 *         &lt;!-- all dependency exclusions must be defined in dependency managment --&gt;
 *         &lt;manageExclusions&gt;true&lt;/manageExclusions&gt;
 *       &lt;/dependencyConfiguration&gt;
 *     &lt;/rules&gt;
 * </pre>
 *
 * @id {@link PedanticEnforcerRule#DEPENDENCY_CONFIGURATION}
 * @since 1.0.0
 */
public class PedanticDependencyConfigurationEnforcer extends AbstractPedanticEnforcer {

  /**
   * If enabled, dependency versions have to be declared in <code>&lt;dependencyManagement&gt;</code>.
   */
  private boolean manageVersions = true;

  /**
   * Allow <code>${project.version}</code> or <code>${version}</code> as dependency version.
   */
  private boolean allowUnmangedProjectVersions = true;

  /**
   * If enabled, dependency exclusions have to be declared in <code>&lt;dependencyManagement&gt;</code>.
   */
  private boolean manageExclusions = true;

  /**
   * If set to <code>true</code>, all dependency versions have to be defined in the dependency management.
   *
   * @param manageVersions Manage dependency versions in the dependency management.
   * @configParam
   * @default <code>true</code>
   * @since 1.0.0
   */
  public void setManageVersions(boolean manageVersions) {
    this.manageVersions = manageVersions;
  }

  /**
   * If set to <code>true</code>, <code><version>${project.version}</version></code> may be used within
   * the dependencies section.
   *
   * @param allowUnmangedProjectVersions Allow project versions outside of the dependencies section.
   * @configParam
   * @default <code>true</code>
   * @since 1.0.0
   */
  public void setAllowUnmanagedProjectVersions(boolean allowUnmangedProjectVersions) {
    this.allowUnmangedProjectVersions = allowUnmangedProjectVersions;
  }

  /**
   * If set to <code>true</code>, all dependency exclusions must be declared in the dependency management.
   *
   * @param manageExclusions Manage exclusion in dependency management.
   * @configParam
   * @default <code>true</code>
   * @since 1.0.0
   */
  public void setManageExclusions(boolean manageExclusions) {
    this.manageExclusions = manageExclusions;
  }

  @Override
  protected PedanticEnforcerRule getDescription() {
    return PedanticEnforcerRule.DEPENDENCY_CONFIGURATION;
  }

  @Override
  protected void accept(PedanticEnforcerVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  protected void doEnforce(ErrorReport report) {
    if (this.manageVersions) {
      enforceManagedVersions(report);
    }
    if (this.manageExclusions) {
      enforceManagedExclusion(report);
    }
  }

  private void enforceManagedVersions(ErrorReport report) {
    Collection<DependencyModel> versionedDependencies = searchForDependencies(DependencyPredicate.WITH_VERSION);

    // Filter all project versions if allowed
    if (this.allowUnmangedProjectVersions) {
      versionedDependencies = versionedDependencies.stream().filter(DependencyPredicate.WITH_PROJECT_VERSION).collect(Collectors.toList());
    }

    if (!versionedDependencies.isEmpty()) {
      report.addLine("Dependency versions have to be declared in <dependencyManagement>:")
          .addLine(toList(versionedDependencies));
    }
  }

  private void enforceManagedExclusion(ErrorReport report) {
    Collection<DependencyModel> depsWithExclusions = searchForDependencies(DependencyPredicate.WITH_EXCLUSION);

    if (!depsWithExclusions.isEmpty()) {
      report.addLine("Dependency exclusions have to be declared in <dependencyManagement>:")
          .addLine(toList(depsWithExclusions));
    }
  }

  private Collection<DependencyModel> searchForDependencies(Predicate<DependencyModel> predicate) {
    List<DependencyModel> dependencies = getProjectModel().getDependencies();
    return dependencies.stream().filter(predicate).collect(Collectors.toList());
  }

  private enum DependencyPredicate implements Predicate<DependencyModel> {
    WITH_VERSION {
      @Override
      public boolean test(DependencyModel input) {
        return input.getVersion() != null;
      }
    },
    WITH_PROJECT_VERSION {
      @Override
      public boolean test(DependencyModel input) {
        return !"${project.version}".equals(input.getVersion())
            && !"${version}".equals(input.getVersion());
      }
    },
    WITH_EXCLUSION {
      @Override
      public boolean test(DependencyModel input) {
        return !input.getExclusions().isEmpty();
      }
    }
  }
}

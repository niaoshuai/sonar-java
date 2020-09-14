/*
 * SonarQube Java
 * Copyright (C) 2012-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks.tests;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.java.model.JavaTree;
import org.sonar.java.model.SyntacticEquivalence;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.SymbolMetadata;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TypeCastTree;

import static java.util.Arrays.asList;

@Rule(key = "S5976")
public class ParameterizedTestCheck extends IssuableSubscriptionVisitor {
  private static final Set<String> TEST_ANNOTATIONS = new HashSet<>(asList(
    "org.junit.Test",
    "org.junit.jupiter.api.Test",
    "org.testng.annotations.Test"));

  private static final int MIN_SIMILAR_METHODS = 3;
  private static final int MIN_NUMBER_LINES = 1;
  private static final int MAX_NUMBER_PARAMETER = 3;

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.CLASS);
  }

  @Override
  public void visitNode(Tree tree) {
    ClassTree classTree = (ClassTree) tree;
    List<MethodTree> methods = classTree.members().stream()
      .filter(member -> member.is(Tree.Kind.METHOD))
      .map(MethodTree.class::cast)
      .filter(ParameterizedTestCheck::isParametrizedCandidate)
      .collect(Collectors.toList());
    if (methods.size() < MIN_SIMILAR_METHODS) {
      return;
    }

    Set<MethodTree> handled = new HashSet<>();
    for (int i = 0; i < methods.size(); i++) {
      MethodTree method = methods.get(i);
      List<StatementTree> methodBody = method.block().body();
      // In addition to filtering literals, we want to count the number of differences since they will represent the number of parameter
      // that would be required to transform the tests to a single parametrized one.
      CollectAndFilter collectAndFilter = new CollectAndFilter();

      List<MethodTree> equivalentMethods = methods.stream()
        .skip(i + 1L)
        // avoid reporting multiple times
        .filter(otherMethod -> !handled.contains(otherMethod))
        // only consider method syntactically equivalent, ignoring literals
        .filter(otherMethod -> SyntacticEquivalence.areEquivalent(methodBody, otherMethod.block().body(), collectAndFilter))
        .collect(Collectors.toList());

      if (equivalentMethods.size() + 1 >= MIN_SIMILAR_METHODS) {
        handled.add(method);
        handled.addAll(equivalentMethods);

        if (collectAndFilter.nodeToParametrize.size() > MAX_NUMBER_PARAMETER) {
          // We don't report an issue if the change would result in too many parameters.
          // We still add it to "handled" to not report a subset of candidate methods.
          return;
        }

        reportIssue(method.simpleName(),
          "Replace these tests with a single Parameterized test.",
          equivalentMethods.stream().map(equivalentMethod ->
            new JavaFileScannerContext.Location("Related test", equivalentMethod.simpleName())).collect(Collectors.toList()),
          null);
      }
    }
  }

  private static boolean isParametrizedCandidate(MethodTree methodTree) {
    BlockTree block = methodTree.block();
    SymbolMetadata symbolMetadata = methodTree.symbol().metadata();
    return block != null &&
      block.body().size() >= MIN_NUMBER_LINES &&
      TEST_ANNOTATIONS.stream().anyMatch(symbolMetadata::isAnnotatedWith);
  }

  static class CollectAndFilter implements BiPredicate<JavaTree, JavaTree> {

    Set<JavaTree> nodeToParametrize = new HashSet<>();

    @Override
    public boolean test(JavaTree leftNode, JavaTree rightNode) {
      if (isLiteral(leftNode) && isLiteral(rightNode)) {
        if (!SyntacticEquivalence.areEquivalent(leftNode, rightNode)) {
          // If the two literals are not equivalent, it means that we will have to create a parameter for it.
          nodeToParametrize.add(leftNode);
        }
        return true;
      }
      return false;
    }

    private static boolean isLiteral(@Nullable JavaTree node) {
      if (node instanceof TypeCastTree) {
        // If the node is a cast of literal, we consider it as literal as well.
        return isLiteral((JavaTree) ((TypeCastTree) node).expression());
      }
      return node instanceof LiteralTree;
    }
  }

}

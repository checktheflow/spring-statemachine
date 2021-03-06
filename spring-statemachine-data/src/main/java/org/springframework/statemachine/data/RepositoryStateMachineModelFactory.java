/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.SpelExpressionAction;
import org.springframework.statemachine.config.model.AbstractStateMachineModelFactory;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.guard.SpelExpressionGuard;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.StringUtils;

/**
 * A generic {@link StateMachineModelFactory} which is backed by a Spring Data
 * Repository abstraction.
 *
 * @author Janne Valkealahti
 *
 */
public class RepositoryStateMachineModelFactory extends AbstractStateMachineModelFactory<String, String>
		implements StateMachineModelFactory<String, String> {

	private final StateRepository<? extends RepositoryState> stateRepository;
	private final TransitionRepository<? extends RepositoryTransition> transitionRepository;

	/**
	 * Instantiates a new repository state machine model factory.
	 *
	 * @param stateRepository the state repository
	 * @param transitionRepository the transition repository
	 */
	public RepositoryStateMachineModelFactory(StateRepository<? extends RepositoryState> stateRepository,
			TransitionRepository<? extends RepositoryTransition> transitionRepository) {
		this.stateRepository = stateRepository;
		this.transitionRepository = transitionRepository;
	}

	@Override
	public StateMachineModel<String, String> build() {
		return build(null);
	}

	@Override
	public StateMachineModel<String, String> build(String machineId) {
		ConfigurationData<String, String> configurationData = new ConfigurationData<>();

		Collection<StateData<String, String>> stateDatas = new ArrayList<>();
		for (RepositoryState s : stateRepository.findByMachineId(machineId)) {

			Collection<Action<String, String>> stateActions = new ArrayList<Action<String, String>>();
			Set<? extends RepositoryAction> repositoryStateActions = s.getStateActions();
			if (repositoryStateActions != null) {
				for (RepositoryAction repositoryAction : repositoryStateActions) {
					Action<String, String> action = null;
					if (StringUtils.hasText(repositoryAction.getName())) {
						action = resolveAction(repositoryAction.getName());
					} else if (StringUtils.hasText(repositoryAction.getSpel())) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

						action = new SpelExpressionAction<String, String>(parser.parseExpression(repositoryAction.getSpel()));
					}
					if (action != null) {
						stateActions.add(action);
					}
				}
			}

			Collection<Action<String, String>> entryActions = new ArrayList<Action<String, String>>();
			Set<? extends RepositoryAction> repositoryEntryActions = s.getEntryActions();
			if (repositoryEntryActions != null) {
				for (RepositoryAction repositoryAction : repositoryEntryActions) {
					Action<String, String> action = null;
					if (StringUtils.hasText(repositoryAction.getName())) {
						action = resolveAction(repositoryAction.getName());
					} else if (StringUtils.hasText(repositoryAction.getSpel())) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

						action = new SpelExpressionAction<String, String>(parser.parseExpression(repositoryAction.getSpel()));
					}
					if (action != null) {
						stateActions.add(action);
					}
				}
			}

			Collection<Action<String, String>> exitActions = new ArrayList<Action<String, String>>();
			Set<? extends RepositoryAction> repositoryExitActions = s.getExitActions();
			if (repositoryExitActions != null) {
				for (RepositoryAction repositoryAction : repositoryExitActions) {
					Action<String, String> action = null;
					if (StringUtils.hasText(repositoryAction.getName())) {
						action = resolveAction(repositoryAction.getName());
					} else if (StringUtils.hasText(repositoryAction.getSpel())) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

						action = new SpelExpressionAction<String, String>(parser.parseExpression(repositoryAction.getSpel()));
					}
					if (action != null) {
						stateActions.add(action);
					}
				}
			}

			StateData<String,String> stateData = new StateData<String, String>(s.getParentState(), null, s.getState(), s.isInitial());
			stateData.setStateActions(stateActions);
			stateData.setEntryActions(entryActions);
			stateData.setExitActions(exitActions);
			stateDatas.add(stateData);
		}
		StatesData<String, String> statesData = new StatesData<>(stateDatas);

		Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
		for (RepositoryTransition t : transitionRepository.findByMachineId(machineId)) {

			Collection<Action<String, String>> actions = new ArrayList<Action<String, String>>();
			Set<? extends RepositoryAction> repositoryActions = t.getActions();
			if (repositoryActions != null) {
				for (RepositoryAction repositoryAction : repositoryActions) {
					Action<String, String> action = null;
					if (StringUtils.hasText(repositoryAction.getName())) {
						action = resolveAction(repositoryAction.getName());
					} else if (StringUtils.hasText(repositoryAction.getSpel())) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

						action = new SpelExpressionAction<String, String>(parser.parseExpression(repositoryAction.getSpel()));
					}
					if (action != null) {
						actions.add(action);
					}
				}
			}

			TransitionKind kind = t.getKind();

			Guard<String, String> guard = null;
			RepositoryGuard repositoryGuard = t.getGuard();
			if (repositoryGuard != null) {
				if (StringUtils.hasText(repositoryGuard.getName())) {
					guard = resolveGuard(repositoryGuard.getName());
				} else if (StringUtils.hasText(repositoryGuard.getSpel())) {
					SpelExpressionParser parser = new SpelExpressionParser(
							new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
					guard = new SpelExpressionGuard<>(parser.parseExpression(repositoryGuard.getSpel()));
				}
			}

			transitionData.add(new TransitionData<>(t.getSource(), t.getTarget(), t.getEvent(), actions, guard, kind != null ? kind : TransitionKind.EXTERNAL));
		}
		TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);

		StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData, transitionsData);
		return stateMachineModel;
	}
}

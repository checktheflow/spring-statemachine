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
package org.springframework.statemachine.data.jpa;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.init.Jackson2RepositoryPopulatorFactoryBean;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.RepositoryStateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.statemachine.transition.TransitionKind;

public class JpaRepositoryTests extends AbstractJpaRepositoryTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testRepository1() {
		context.register(Config.class);
		context.refresh();

		JpaStateRepository statesRepository = context.getBean(JpaStateRepository.class);
		JpaRepositoryState state = new JpaRepositoryState("S1");
		statesRepository.save(state);
		Iterable<JpaRepositoryState> findAll = statesRepository.findAll();
		assertThat(findAll.iterator().next().getState(), is("S1"));

		JpaTransitionRepository transitionsRepository = context.getBean(JpaTransitionRepository.class);
		JpaRepositoryTransition transition = new JpaRepositoryTransition("S1", "S2", "E1");
		transition.setKind(TransitionKind.EXTERNAL);
		transitionsRepository.save(transition);
		JpaRepositoryTransition transition2 = transitionsRepository.findAll().iterator().next();
		assertThat(transition2.getSource(), is("S1"));
		assertThat(transition2.getTarget(), is("S2"));
		assertThat(transition2.getEvent(), is("E1"));
		assertThat(transition2.getKind(), is(TransitionKind.EXTERNAL));

		context.close();
	}

	@Test
	public void testRepository2() {
		context.register(Config.class);
		context.refresh();

		@SuppressWarnings("unchecked")
		StateRepository<JpaRepositoryState> statesRepository1 = context.getBean(StateRepository.class);
		JpaRepositoryState state = new JpaRepositoryState("S1");
		statesRepository1.save(state);
		@SuppressWarnings("unchecked")
		StateRepository<? extends RepositoryState> statesRepository2 = context.getBean(StateRepository.class);
		Iterable<? extends RepositoryState> findAll = statesRepository2.findAll();
		assertThat(findAll.iterator().next().getState(), is("S1"));

		@SuppressWarnings("unchecked")
		TransitionRepository<RepositoryTransition> transitionsRepository = context.getBean(TransitionRepository.class);
		RepositoryTransition transition = new JpaRepositoryTransition("S1", "S2", "E1");
		transitionsRepository.save(transition);
		RepositoryTransition transition2 = transitionsRepository.findAll().iterator().next();
		assertThat(transition2.getSource(), is("S1"));
		assertThat(transition2.getTarget(), is("S2"));
		assertThat(transition2.getEvent(), is("E1"));

		context.close();
	}

	@Test
	public void testRepository3() {
		context.register(Config.class);
		context.refresh();

		JpaStateRepository statesRepository = context.getBean(JpaStateRepository.class);
		JpaRepositoryState state1 = new JpaRepositoryState("machine1", "S1", true);
		statesRepository.save(state1);
		JpaRepositoryState state2 = new JpaRepositoryState("machine2", "S2", false);
		statesRepository.save(state2);

		List<JpaRepositoryState> findByMachineId1 = statesRepository.findByMachineId("machine1");
		List<JpaRepositoryState> findByMachineId2 = statesRepository.findByMachineId("machine2");
		assertThat(findByMachineId1.size(), is(1));
		assertThat(findByMachineId2.size(), is(1));
		assertThat(findByMachineId1.get(0).getMachineId(), is("machine1"));
		assertThat(findByMachineId2.get(0).getMachineId(), is("machine2"));


		JpaTransitionRepository transitionsRepository = context.getBean(JpaTransitionRepository.class);
		JpaRepositoryTransition transition1 = new JpaRepositoryTransition("machine1", "S1", "S2", "E1");
		JpaRepositoryTransition transition2 = new JpaRepositoryTransition("machine2", "S3", "S4", "E2");
		transitionsRepository.save(transition1);
		transitionsRepository.save(transition2);
		List<JpaRepositoryTransition> findByMachineId3 = transitionsRepository.findByMachineId("machine1");
		List<JpaRepositoryTransition> findByMachineId4 = transitionsRepository.findByMachineId("machine2");

		assertThat(findByMachineId3.size(), is(1));
		assertThat(findByMachineId4.size(), is(1));
		assertThat(findByMachineId3.get(0).getMachineId(), is("machine1"));
		assertThat(findByMachineId4.get(0).getMachineId(), is("machine2"));

		context.close();
	}

	@Test
	public void testRepository4() {
		context.register(Config.class);
		context.refresh();

		JpaActionRepository actionsRepository = context.getBean(JpaActionRepository.class);
		JpaRepositoryAction action1 = new JpaRepositoryAction();
		action1.setSpel("spel1");
		action1.setName("action1");
		actionsRepository.save(action1);

		assertThat(actionsRepository.count(), is(1l));
		JpaRepositoryAction action11 = actionsRepository.findAll().iterator().next();
		assertThat(action1.getSpel(), is(action11.getSpel()));
		assertThat(action1.getName(), is(action11.getName()));
	}

	@Test
	public void testRepository5() {
		context.register(Config.class);
		context.refresh();

		JpaActionRepository actionsRepository = context.getBean(JpaActionRepository.class);

		JpaTransitionRepository transitionsRepository = context.getBean(JpaTransitionRepository.class);
		JpaRepositoryTransition transition = new JpaRepositoryTransition("S1", "S2", "E1");

		JpaRepositoryAction action1 = new JpaRepositoryAction();
		action1.setName("action1");

		Set<JpaRepositoryAction> actions = new HashSet<>(Arrays.asList(action1));
		transition.setActions(actions);

		transitionsRepository.save(transition);
		JpaRepositoryTransition transition2 = transitionsRepository.findAll().iterator().next();
		assertThat(transition2.getSource(), is("S1"));
		assertThat(transition2.getTarget(), is("S2"));
		assertThat(transition2.getEvent(), is("E1"));

		assertThat(actionsRepository.count(), is(1l));
		JpaRepositoryAction action11 = actionsRepository.findAll().iterator().next();
		assertThat(action1.getName(), is(action11.getName()));


		assertThat(transition2.getActions().size(), is(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine2() throws Exception {
		context.register(Config2.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2").and()
					.step().sendEvent("E2").expectStates("S3").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine3() throws Exception {
		context.register(Config3.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2", "S20").and()
					.step().sendEvent("E2").expectStates("S2", "S21").and()
					.step().sendEvent("E3").expectStates("S1").and()
					.step().sendEvent("E4").expectStates("S2", "S21").and()
					.build();
		plan.test();
	}

	@Test
	public void testAutowire() {
		context.register(Config.class, WireConfig.class);
		context.refresh();
	}

	@EnableAutoConfiguration
	static class Config {
	}

	@Configuration
	static class WireConfig {

		@Autowired
		StateRepository<JpaRepositoryState> statesRepository1;

		@Autowired
		TransitionRepository<JpaRepositoryTransition> statesRepository11;

		@SuppressWarnings("rawtypes")
		@Autowired
		StateRepository statesRepository2;

		@Autowired
		JpaStateRepository statesRepository3;

		@Autowired
		StateRepository<? extends RepositoryState> statesRepository4;
	}

	@EnableAutoConfiguration
	static class Config2 {

		@Bean
		public Jackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			Jackson2RepositoryPopulatorFactoryBean factoryBean = new Jackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data2.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config3 {

		@Bean
		public Jackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			Jackson2RepositoryPopulatorFactoryBean factoryBean = new Jackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data3.json")});
			return factoryBean;
		}
	}

	@Configuration
	@EnableStateMachineFactory
	public static class FactoryConfig extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private StateRepository<? extends RepositoryState> stateRepository;

		@Autowired
		private TransitionRepository<? extends RepositoryTransition> transitionRepository;

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new RepositoryStateMachineModelFactory(stateRepository, transitionRepository);
		}
	}

}

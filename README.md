# black-box-testing

This repository demonstrates an implementation-agnostic approach for testing (micro)services.

## Why?

We want to test the behavior of the service, not its implementation.
Oftentimes the current-day microservices strive to be small and simple. Yet I often see them only heavily unit-tested using
mostly mock-driven development, which provides little assurance that the fully assembled component will behave (or even start) as expected.

Unit tests are great and should absolutely be used for testing the isolated pieces, computations, algorithms.
Mocking frameworks can be useful for ensuring some orchestration is behaving as expected. Although those are easily abused when used blindly
which then results in implementation-focused tests that provide great coverage percentage on paper, but otherwise do more harm than good
by coupling the tests with the implementation, not focusing on behavior, and missing the actual and critical integration points.

Better than mock-driven unit tests are of course some form of integration tests that hit the service's APIs and have some of the
upstream dependencies mocked out. Still, those often only test partial assemblies and are often tightly coupled to
the framework that was used to implement the logic (like Spring's MockMVC). What is even worse - those tests often use
different "profiles" for testing and for production assembly, which effectively results in one thing being tested
and another thing then being assembled and executed in production.

I think we can do better.

## How?

Assemble the service in a way that it will go to production. Nowadays, it is usually a docker image that gets published.
It is that docker image that contains the full assembly and this is what we want to test.
We need to be sure that it is able to start (so many times I've seen all the unit and mock tests pass, yet the service
is unable to start due to some untested wiring) and that it behaves the way we want it to behave as a whole.

Stub out the external dependencies using other containers (use testcontainers):
- For databases use the actual database containers or their lightweight alpine counterparts
 (do make sure that the data is written to tmpfs, this makes the tests a lot faster)
- For any HTTP dependencies, use Wiremock containers
- For RabbitMQ, use RabbitMQ containers
- For SQS, use ElasticMQ
- Kafka is also doable, but I haven't used it in a while now
- etc etc

Treat the service under test as a black box in the tests - hit it over the API. This can either be an HTTP endpoint, or
a message that gets published to a message broker and which the service then consumes.
Assert the outcomes:
- if it was some state mutation, is there another service endpoint to query and assert? Query this endpoint.
- should there have been a message published to a broker? Query the broker.
- was an external service supposed to be hit? Assert the interaction happened on the wiremock container.
- worst case - query the database and see the data that we expected has appeared there.

Note that once you have a solid black box test coverage, the implementation details become way less relevant and
can be easily altered while maintaining the behavior. Things like frameworks also become pure implementation details
and can be updated/changed, or perhaps removed without having to rewrite the tests.

I'd argue that the simplest services (like ones with REST APIs that write to databases) do not need any of the unit tests
and a set of black box tests for the behavior are the way to go.

### Is this slow?

It is a bit slower than executing pure unit tests. For the test suite to start, all the dependency stubs have to start up as well.
This of course depends on the complexity of the service under test and on the amount of external dependencies that it has.


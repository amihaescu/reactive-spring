package com.example.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class DataApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataApplication.class, args);
	}

}

@Component
@RequiredArgsConstructor
@Log4j2
class SampleDataInitializer{

	private final ReservationRepository reservationRepository;
	private final DatabaseClient databaseClient;

	@EventListener(ApplicationReadyEvent.class)
	public void ready() {

		this.databaseClient
				.select().from("reservation").as(Reservation.class)
				.fetch()
				.all()
				.doOnComplete( () -> {log.info("---------------------------");})
				.subscribe(log::info);

		Flux<Reservation> reservations = Flux.just("Madhura", "Josh", "Olga", "Marcin", "Ria", "Stefan", "Violetta", "Dr. Syer")
					.map(name -> new Reservation(null, name))
					.flatMapSequential(reservationRepository::save);

		this.reservationRepository
				.deleteAll()
				.thenMany(reservations)
				.thenMany(this.reservationRepository.findAll())
				.subscribe(log::info);
	}

}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer>{
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Reservation {

	@Id
	private Integer id;
	private String name;
}
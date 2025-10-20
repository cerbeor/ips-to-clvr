package org.immregitries.clvr;

import com.syadem.nuva.Code;
import com.syadem.nuva.NUVA;
import com.syadem.nuva.SupportedLocale;
import com.syadem.nuva.Vaccine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NUVAService {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NUVA_CVX_NOMENCLATURE = "CVX";

	private NUVA nuva;

	public NUVAService() throws IOException {
		nuva = NUVA.load(SupportedLocale.English);
	}

	public Optional<Vaccine> findByCvx(String cvxCode) {
		return findByCode(cvxCode, NUVA_CVX_NOMENCLATURE);
	}


	public Optional<Vaccine> findByCode(String vaccineCode, String nomenclature) {
		Stream<Vaccine> vaccineStream = StreamSupport.stream(nuva.getVaccineRepository().spliterator(), true);
		return vaccineStream.filter(vaccine -> {
			Optional<Code> cvx = vaccine.getCodesList().stream()
				.filter(code -> nomenclature.equals(code.getNomenclature()) && vaccineCode.equals(code.getValue())).findFirst();
			return cvx.isPresent();
		}).findFirst();
	}
}

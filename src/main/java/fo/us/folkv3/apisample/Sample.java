package fo.us.folkv3.apisample;

import fo.us.folkv3.api.cert.CertificateConfig;
import fo.us.folkv3.api.cert.SecurityContext;
import fo.us.folkv3.api.client.*;
import fo.us.folkv3.api.model.*;
import fo.us.folkv3.api.model.param.AddressParam;
import fo.us.folkv3.api.model.param.NameParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Sample {

	private final HeldinConfig heldinConfig;
	private final CertificateConfig certificateConfig;
	private PersonSmallClient smallClient;
	private PersonMediumClient mediumClient;
	private PrivateCommunityClient privateCommunityClient;
	private PublicCommunityClient publicCommunityClient;

	public Sample(HeldinConfig heldinConfig) {
		this(heldinConfig, null);
	}

	public Sample(HeldinConfig heldinConfig, CertificateConfig certificateConfig) {
		this.heldinConfig = heldinConfig;
		this.certificateConfig = certificateConfig;
	}

	public static void main(String[] args) {
		var heldinConfig = HeldinConfig.secureHost("10.20.30.40")
				.fo().test() // Use .fo() only for production
				.com() // Or .gov()
				.memberCode("123456")
				.subSystemCode("my-system")
				.withUserId("my-system-id"); // Optional userId
		// Or like this
//		var heldinConfig = HeldinConfig.of("10.20.30.40", true, "FO-TST/COM/123456/my-system");

		// Load certificate configuration from VM or ENV properties
		//   VM name                        ENV name
		//   folkv3.tlsProtocol             FOLKV3_TLSPROTOCOL             optional
		//   folkv3.clientKeyStore.type     FOLKV3_CLIENTKEYSTORE_TYPE     optional, default value PKCS12
		//   folkv3.clientKeyStore.path     FOLKV3_CLIENTKEYSTORE_PATH
		//   folkv3.clientKeyStore.password FOLKV3_CLIENTKEYSTORE_PASSWORD
		//   folkv3.serverCertificate.path  FOLKV3_SERVERCERTIFICATE_PATH  optional, default trust all
		//
        //   VM properties
		//   -Dfolkv3.tlsProtocol=TLSv13 -Dfolkv3.clientKeyStore.type=PKCS12 -Dfolkv3.clientKeyStore.path=/path/to/client-cert.pfx -Dfolkv3.clientKeyStore.password=??? -Dfolkv3.serverCertificate.path=/path/to/server-cert.cer
		//   ENV properties
		//   FOLKV3_TLSPROTOCOL=TLSv13; FOLKV3_CLIENTKEYSTORE_TYPE=PKCS12; FOLKV3_CLIENTKEYSTORE_PATH=/path/to/client-cert.pfx; FOLKV3_CLIENTKEYSTORE_PASSWORD=???; FOLKV3_SERVERCERTIFICATE_PATH=/path/to/server-cert.cer;
//		var certificateConfig = CertificateConfig.loadClientCertificate();
//		var certificateConfig = CertificateConfig.loadServerCertificate();
//		var certificateConfig = CertificateConfig.loadClientAndServerCertificate();

		var certificateConfig = CertificateConfig.builder()
				.clientKeyStorePath("/path/to/client-cert.pfx") // Required if the X-Road client (SUBSYSTEM:FO-TST/COM/123456/my-system) specifies HTTPS
				.clientKeyStorePassword("???")
				.clientKeyStoreType(SecurityContext.KeyStoreType.PKCS12) // Optional, default value PKCS12
				.serverCertificatePath("/path/to/server-cert.cer") // Optional, default trust all
				.tlsProtocol(SecurityContext.TlsProtocol.TLSv13) // Optional, default value TLSv13
				.build();

		var sample = new Sample(heldinConfig, certificateConfig);
		// Use this if there is no client certificate and/or you trust server certificate
//		var sample = new Sample(heldinConfig);

		sample.testGetPersonMediumByPtal();
	}

	private void call(Runnable method) {
		try {
			method.run();
		} catch (FolkApiException e) {
			System.out.println("Error: " + e.getMessage());
			System.out.println();
		}
	}
	
	private void testSmallMethods() {
		call(() -> testGetPersonSmallByPrivateId());
		call(() -> testGetPersonSmallByPtal());
		call(() -> testGetPersonSmallByNameAndAddress());
		call(() -> testGetPersonSmallByNameAndDateOfBirth());
	}

	private void testMediumMethods() {
		call(() -> testGetPersonMediumByPrivateId());
		call(() -> testGetPersonMediumByPublicId());
		call(() -> testGetPersonMediumByPtal());
		call(() -> testGetPersonMediumByNameAndAddress());
		call(() -> testGetPersonMediumByNameAndDateOfBirth());
	}

	private void testPrivateCommunityMethods() {
		call(() -> testGetPrivateChanges());
		call(() -> testAddPersonToCommunityByNameAndAddress());
		call(() -> testAddPersonToCommunityByNameAndDateOfBirth());
		call(() -> testRemovePersonFromCommunity());
		call(() -> testRemovePersonsFromCommunity());
	}

	private void testPublicCommunityMethods() {
		call(() -> testGetPublicChanges());
	}

	private PersonSmallClient smallClient() {
		if (smallClient == null) {
			smallClient = certificateConfig == null
					? FolkClient.personSmall(heldinConfig)
					: FolkClient.personSmall(heldinConfig, certificateConfig);
		}
		return smallClient;
	}
	
	private PersonMediumClient mediumClient() {
		if (mediumClient == null) {
			mediumClient = certificateConfig == null
					? FolkClient.personMedium(heldinConfig)
					: FolkClient.personMedium(heldinConfig, certificateConfig);
		}
		return mediumClient;
	}

	private PrivateCommunityClient privateCommunityClient() {
		if (privateCommunityClient == null) {
			privateCommunityClient = certificateConfig == null
					? FolkClient.privateCommunity(heldinConfig)
					: FolkClient.privateCommunity(heldinConfig, certificateConfig);
		}
		return privateCommunityClient;
	}

	private PublicCommunityClient publicCommunityClient() {
		if (publicCommunityClient == null) {
			publicCommunityClient = certificateConfig == null
					? FolkClient.publicCommunity(heldinConfig)
					: FolkClient.publicCommunity(heldinConfig, certificateConfig);
		}
		return publicCommunityClient;
	}

	
	// Test small methods
	
	private void testSmallGetMyPrivileges() {
		System.out.println("# testSmallGetMyPrivileges");
		smallClient().getMyPrivileges().forEach(System.out::println);;
	}
	
	private void testGetPersonSmallByPrivateId() {
		System.out.println("# testGetPersonSmallByPrivateId");
		var person = smallClient().getPerson(
				PrivateId.of(1)
				);
		printPerson(person);
	}

	private void testGetPersonSmallByPtal() {
		System.out.println("# testGetPersonSmallByPtal");
		var person = smallClient().getPerson(
				Ptal.of("300408-559")
				);
		printPerson(person);
	}

	private void testGetPersonSmallByNameAndAddress() {
		System.out.println("# testGetPersonSmallByNameAndAddress");
		var person = smallClient().getPerson(
				NameParam.of("Karius", "Davidsen"),
				AddressParam.of("Úti í Bø",
						HouseNumber.of(16),
						"Syðrugøta")
				);
		printPerson(person);
	}

	private void testGetPersonSmallByNameAndDateOfBirth() {
		System.out.println("# testGetPersonSmallByNameAndDateOfBirth");
		var person = smallClient().getPerson(
				NameParam.of("Karius", "Davidsen"),
				LocalDate.of(2008, 4, 30)
				);
		printPerson(person);
	}

	
	// Test medium methods
	
	private void testMediumGetMyPrivileges() {
		System.out.println("# testMediumGetMyPrivileges");
		mediumClient().getMyPrivileges().forEach(System.out::println);;
	}
	
	private void testGetPersonMediumByPrivateId() {
		System.out.println("# testGetPersonMediumByPrivateId");
		var person = mediumClient().getPerson(
				PrivateId.of(1)
				);
		printPerson(person);
	}

	private void testGetPersonMediumByPublicId() {
		System.out.println("# testGetPersonMediumByPublicId");
		var person = mediumClient().getPerson(
				PublicId.of(1157442)
				);
		printPerson(person);
	}

	private void testGetPersonMediumByPtal() {
		System.out.println("# testGetPersonMediumByPtal");
		var person = mediumClient().getPerson(
				Ptal.of("300408559")
				);
		printPerson(person);
	}

	private void testGetPersonMediumByNameAndAddress() {
		System.out.println("# testGetPersonMediumByNameAndAddress");
		var person = mediumClient().getPerson(
				NameParam.of("Karius", "Davidsen"),
				AddressParam.of("Úti í Bø",	HouseNumber.of(16),	"Syðrugøta")
				);
		printPerson(person);
	}

	private void testGetPersonMediumByNameAndDateOfBirth() {
		System.out.println("# testGetPersonMediumByNameAndDateOfBirth");
		var person = mediumClient().getPerson(
				NameParam.of("Karius", "Davidsen"),
				LocalDate.of(2008, 4, 30)
				);
		printPerson(person);
	}

	
	// Test community methods
	
	private void testGetPrivateChanges() {
		System.out.println("# testGetPrivateChanges");
		Changes<PrivateId> changes = privateCommunityClient().getChanges(LocalDateTime.now().minusWeeks(1));
		System.out.printf("Changes - from: %s; to: %s; ids: %s%n%n", changes.getFrom(), changes.getTo(), changes.getIds());
	}
	
	private void testGetPublicChanges() {
		System.out.println("# testGetPublicChanges");
		Changes<PublicId> changes = publicCommunityClient().getChanges(LocalDateTime.now().minusWeeks(1));
		System.out.printf("Changes - from: %s; to: %s; ids: %s%n%n", changes.getFrom(), changes.getTo(), changes.getIds());
	}

	private void testAddPersonToCommunityByNameAndAddress() {
		System.out.println("# testAddPersonToCommunityByNameAndAddress");
		var communityPerson = privateCommunityClient().addPersonToCommunity(
				NameParam.of("Karius", "Davidsen"),
				AddressParam.of("Úti í Bø",
						HouseNumber.of(16),
						"Syðrugøta")
				);
		printCommunityPerson(communityPerson);
	}

	private void testAddPersonToCommunityByNameAndDateOfBirth() {
		System.out.println("# testAddPersonToCommunityByNameAndDateOfBirth");
		var communityPerson = privateCommunityClient().addPersonToCommunity(
				NameParam.of("Karius", "Davidsen"),
				LocalDate.of(2008, 4, 30)
				);
		printCommunityPerson(communityPerson);
	}

	private void testRemovePersonFromCommunity() {
		System.out.println("# testRemovePersonFromCommunity");
		var removedId = privateCommunityClient().removePersonFromCommunity(PrivateId.of(1));
		System.out.printf("Removed id: %s%n%n", removedId);
	}
	
	private void testRemovePersonsFromCommunity() {
		System.out.println("# testRemovePersonsFromCommunity");
		var removedIds = privateCommunityClient().removePersonsFromCommunity(PrivateId.list(1, 2, 3));
		System.out.printf("Removed ids: %s%n%n", removedIds);
	}
	

	// Print methods
	
	private static void printPerson(PersonSmall person) {
		if (person == null) {
			System.out.println("Person was not found!");
		} else {
			System.out.println(personToString(person));
		}
		System.out.println();
	}

	private static void printCommunityPerson(CommunityPerson person) {
		if (person == null) {
			System.out.println("Oops!");
		} else {
			System.out.println(communityPersonToString(person));
		}
		System.out.println();
	}

	private static String personToString(PersonSmall person) {
		if (person instanceof PersonMedium) {
			var personPublic = (PersonMedium) person;
			return format(person.getPrivateId(), personPublic.getPublicId(), ptal(personPublic),
					person.getName(), addressToString(person), personPublic.getDateOfBirth(),
					civilStatusToString(personPublic), specialMarksToString(personPublic),
					incapacityToString(personPublic));
		}
		var deadOrAlive = person.isAlive() ? "ALIVE" : ("DEAD " + person.getDateOfDeath());
		return format(person.getPrivateId(), person.getName(), addressToString(person), deadOrAlive);
	}

	private static String communityPersonToString(CommunityPerson communityPerson) {
		String personString = null;
		if (communityPerson.getStatus().isAdded()) {
			personString = personToString(communityPerson.getPerson());
		}
		return format(communityPerson.getStatus(), communityPerson.getExistingId(), personString);
	}
	
	private static String addressToString(PersonSmall person) {
		return addressToString(person.getAddress());
	}

	private static String addressToString(Address address) {
		return address.hasStreetAndNumbers()
						? address.getStreetAndNumbers()
								+ "; " + address.getCountry().getCode() + address.getPostalCode()
								+ " " + address.getCity()	
								+ "; " + address.getCountry().getNameFo()
								+ " (from: " + address.getFrom() + ')'
						: null;
	}
	
	private static String civilStatusToString(PersonMedium person) {
		if (person.getCivilStatus() == null) {
			return null;
		}
		return person.getCivilStatus().getType() + ", " + person.getCivilStatus().getFrom();
	}

	private static String ptal(PersonMedium person) {
		return person.getPtal() == null ? null : person.getPtal().getFormattedValue();
	}
	
	private static String specialMarksToString(PersonMedium person) {
		return person.getSpecialMarks().isEmpty()
				? null : person.getSpecialMarks().stream().map(Object::toString).collect(Collectors.joining());
	}

	private static String incapacityToString(PersonMedium person) {
		if (person.getIncapacity() == null) {
			return null;
		}
		var guardian1 = guardianToString(person.getIncapacity().getGuardian1());
		var guardian2 = guardianToString(person.getIncapacity().getGuardian2());
		return guardian2 == null ? guardian1 : guardian1 + " / " + guardian2;
	}

	private static String guardianToString(Guardian guardian) {
		if (guardian == null) {
			return null;
		}
		return guardian.getName() + " - " + addressToString(guardian.getAddress());
	}
	
	private static String format(Object... values) {
		return Arrays.stream(values)
				.map(v -> v == null ? "-" : v.toString())
				.collect(Collectors.joining(" | "));
	}
	
}

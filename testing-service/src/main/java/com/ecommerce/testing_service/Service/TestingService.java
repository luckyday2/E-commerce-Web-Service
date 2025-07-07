    package com.ecommerce.testing_service.Service;

    import java.util.List;
    import java.util.stream.Collectors;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Service;

    import com.ecommerce.testing_service.DTO.TestingCreate;
    import com.ecommerce.testing_service.DTO.TestingResponse;
    import com.ecommerce.testing_service.Mapper.TestingMapper;
    import com.ecommerce.testing_service.Model.TestingData;
    import com.ecommerce.testing_service.Repository.TestingRepository;

    @Service
    public class TestingService {
        @Autowired
        private TestingRepository testingRepository;

        private String getCurrentUsername() {
            return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        private String getCurrentUserRole() {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse("USER"); // default fallback
        }

        private boolean isCashier() {
            return getCurrentUserRole().equals("KASIR");
        }

        private boolean isUser() {
            return getCurrentUserRole().equals("USER");
        }

        public TestingResponse createTesting(TestingCreate dto) {
            // ✅ Ambil username dari token
            String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // ✅ Map ke entity
            TestingData entity = TestingMapper.toEntity(dto);
            entity.setCreatedBy(username); // 👈 simpan siapa user-nya

            // ✅ Simpan ke DB
            TestingData saved = testingRepository.save(entity);
            return TestingMapper.toResponse(saved);
        }

        // Read all private user
        public List<TestingResponse> getAllTestingByUser() {
            String username = getCurrentUsername();

            return testingRepository.findAll().stream()
                    .filter(data -> username.equals(data.getCreatedBy())) // ✅ now safe
                    .map(TestingMapper::toResponse)
                    .collect(Collectors.toList());
        }

        // Read all
        public List<TestingResponse> getAllTesting() {
            String username = getCurrentUsername();

            if (isUser()) {
                return testingRepository.findAll().stream()
                        .filter(data -> username.equals(data.getCreatedBy())) // 👈 aman dari NullPointer
                        .map(TestingMapper::toResponse)
                        .collect(Collectors.toList());
            }

            // Kasir & Admin
            return testingRepository.findAll().stream()
                    .map(TestingMapper::toResponse)
                    .collect(Collectors.toList());
        }

        // Read by ID
        public TestingResponse getById(int id) {
            TestingData data = testingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("TestingData not found with id: " + id));
            return TestingMapper.toResponse(data);
        }

        // Delete
        public void deleteById(int id) {
            String username = getCurrentUsername();
            TestingData data = testingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));

            if (isUser() && !data.getCreatedBy().equals(username)) {
                throw new RuntimeException("Kamu tidak punya akses hapus data ini");
            }

            if (isCashier()) {
                throw new RuntimeException("KASIR tidak boleh menghapus data");
            }

            testingRepository.deleteById(id);
        }

        // Update jika User hanya update data nya sendiri
        public TestingResponse updateTesting(int id, TestingCreate dto) {
            String username = getCurrentUsername();

            TestingData existing = testingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Data tidak ditemukan untuk diupdate."));

            if (isUser()) {
                // ❌ Hanya boleh update data miliknya sendiri
                if (!existing.getCreatedBy().equals(username)) {
                    throw new RuntimeException("Kamu tidak memiliki izin untuk mengupdate data ini.");
                }

                // ✅ USER tidak boleh update data, return error
                throw new RuntimeException("USER tidak diizinkan untuk mengupdate data.");
            }

            if (isCashier()) {
                // ✅ Kasir hanya boleh update status
                existing.setStatus(dto.getStatus());
            } else {
                // ✅ Admin boleh update semua
                existing.setName(dto.getName());
                existing.setDescription(dto.getDescription());
                existing.setStatus(dto.getStatus());
            }

            TestingData updated = testingRepository.save(existing);
            return TestingMapper.toResponse(updated);
        }

    }

package com.example.internhub_be.service;

import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.domain.MicroTask.MicroTaskStatus;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.MicroTaskResponse;
import com.example.internhub_be.payload.TaskSubmissionRequest;
import com.example.internhub_be.repository.MicroTaskRepository;
import com.example.internhub_be.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InternTaskServiceImpl implements InternTaskService {

    private final MicroTaskRepository microTaskRepository;
    private final UserRepository userRepository;

    public InternTaskServiceImpl(MicroTaskRepository microTaskRepository,
                                 UserRepository userRepository) {
        this.microTaskRepository = microTaskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<MicroTaskResponse> getMyTasks(String internEmail) {
        User intern = getUserByEmail(internEmail);
        return microTaskRepository.findByIntern(intern)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public MicroTaskResponse getMyTaskById(Long taskId, String internEmail) {
        User intern = getUserByEmail(internEmail);
        MicroTask task = getTaskById(taskId);
        validateInternOwnership(task, intern);
        return mapToResponse(task);
    }

    @Override
    @Transactional
    public MicroTaskResponse submitTask(Long taskId, TaskSubmissionRequest request, String internEmail) {
        User intern = getUserByEmail(internEmail);
        MicroTask task = getTaskById(taskId);
        validateInternOwnership(task, intern);

        if (task.getStatus() != MicroTaskStatus.Todo && task.getStatus() != MicroTaskStatus.In_Progress) {
            throw new IllegalStateException(
                "Không thể nộp bài cho task ở trạng thái: " + task.getStatus() +
                ". Chỉ được nộp khi task ở trạng thái Todo hoặc In_Progress."
            );
        }

        task.setSubmissionLink(request.getSubmissionLink());
        task.setSubmissionNote(request.getSubmissionNote());
        task.setStatus(MicroTaskStatus.Submitted);

        return mapToResponse(microTaskRepository.save(task));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private MicroTask getTaskById(Long taskId) {
        return microTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("MicroTask", "id", taskId));
    }

    private void validateInternOwnership(MicroTask task, User intern) {
        if (task.getIntern() == null || !task.getIntern().getId().equals(intern.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập task này.");
        }
    }

    public MicroTaskResponse mapToResponse(MicroTask task) {
        MicroTaskResponse r = new MicroTaskResponse();
        r.setId(task.getId());
        r.setTitle(task.getTitle());
        r.setDescription(task.getDescription());
        r.setDeadline(task.getDeadline());
        r.setStatus(task.getStatus());
        r.setSubmissionLink(task.getSubmissionLink());
        r.setSubmissionNote(task.getSubmissionNote());
        r.setCreatedAt(task.getCreatedAt());
        if (task.getMentor() != null) {
            r.setMentorId(task.getMentor().getId());
            r.setMentorName(task.getMentor().getName());
        }
        if (task.getIntern() != null) {
            r.setInternId(task.getIntern().getId());
            r.setInternName(task.getIntern().getName());
        }
        return r;
    }
}
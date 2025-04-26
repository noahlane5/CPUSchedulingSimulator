import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class CPUScheduler {
    public static void fcfs(int numProc, int pid_proc[], int bt_proc[], int ar_proc[], int ct_proc[], int ta_proc[], int wt_proc[]){
        int temp;
        for (int i = 0; i < numProc; i++){
            for (int j = i+1; j < numProc; j++){
                if(ar_proc[i] > ar_proc[j]){ //this will just check to see if one item arrived first. If it did, it will swap places with the other processes
                    temp = ar_proc[i];
                    ar_proc[i] = ar_proc[j];
                    ar_proc[j] = temp;

                    temp = pid_proc[i];
                    pid_proc[i] = pid_proc[j];
                    pid_proc[j] = temp;

                    temp = bt_proc[i];
                    bt_proc[i] = bt_proc[j];
                    bt_proc[j] = temp;
                }
            }
        }

        System.out.println();
        ct_proc[0] = bt_proc[0] + ar_proc[0];
        for(int i = 1; i < numProc; i++){
            ct_proc[i] = ct_proc[i - 1] + bt_proc[i];   //Completion time = Previous completion + burst time
        }

        double avg_wt = 0;
        double avg_tat = 0;

        for (int i = 0; i < numProc; i++){
            ta_proc[i] = ct_proc[i] - ar_proc[i];
            wt_proc[i] = ta_proc[i] - bt_proc[i];

            avg_wt = avg_wt + wt_proc[i];
            avg_tat = avg_tat + ta_proc[i];
        }

        System.out.println("First Come First Serve");
        System.out.println("Process\t\tAT\t\tBT\t\tCT\t\tTAT\t\tWT");
        for(int i = 0; i < numProc; i++){
            System.out.println(pid_proc[i]+"\t\t\t"+ar_proc[i]+"\t\t"+bt_proc[i]+"\t\t"+ct_proc[i]+"\t\t"+ta_proc[i]+"\t\t"+wt_proc[i]);
        }

        System.out.println();
        System.out.println("Average Wait Time: " + (avg_wt/numProc));
        System.out.println("Average Turn Around Time: " + (avg_tat/numProc));
        printMetrics(numProc, ar_proc, bt_proc, ct_proc);
    }

    public static void sjf(int numProc, int pid_proc[], int bt_proc[], int ar_proc[], int ct_proc[], int ta_proc[], int wt_proc[]) {
        boolean[] finished = new boolean[numProc];
        int[] original_pid = pid_proc.clone(); // preserve original pid order
        int start_time = 0;
        int completed = 0;

        while (completed < numProc) {
            int idx = -1;
            int min_bt = Integer.MAX_VALUE;

            for (int i = 0; i < numProc; i++) {
                if (!finished[i] && ar_proc[i] <= start_time && bt_proc[i] < min_bt) {
                    min_bt = bt_proc[i];
                    idx = i;
                }
            }

            if (idx == -1) {
                start_time++;
            } else {
                ct_proc[idx] = start_time + bt_proc[idx];
                ta_proc[idx] = ct_proc[idx] - ar_proc[idx];
                wt_proc[idx] = ta_proc[idx] - bt_proc[idx];
                start_time = ct_proc[idx];
                finished[idx] = true;
                completed++;
            }
        }

        double avg_wt = 0, avg_tat = 0;
        for (int i = 0; i < numProc; i++) {
            avg_wt += wt_proc[i];
            avg_tat += ta_proc[i];
        }

        System.out.println("Shortest Job First");
        System.out.println("Process\t\tAT\t\tBT\t\tCT\t\tTAT\t\tWT");
        for (int i = 0; i < numProc; i++) {
            System.out.println(original_pid[i] + "\t\t\t" + ar_proc[i] + "\t\t" + bt_proc[i] + "\t\t" + ct_proc[i] + "\t\t" + ta_proc[i] + "\t\t" + wt_proc[i]);
        }

        System.out.println();
        System.out.println("Average Wait Time: " + (avg_wt / numProc));
        System.out.println("Average Turn Around Time: " + (avg_tat / numProc));
        printMetrics(numProc, ar_proc, bt_proc, ct_proc);
    }


    public static void rr(int numProc, int pid_proc[], int bt_proc[], int ar_proc[], int ct_proc[], int ta_proc[], int wt_proc[], int timeQuantum) {
        int[] rem_bt = bt_proc.clone(); // remaining burst times
        int[] temp_ct = new int[numProc];
        int time = 0;
        boolean[] isCompleted = new boolean[numProc];
        int completed = 0;
        Queue<Integer> queue = new LinkedList<>();

        boolean[] inQueue = new boolean[numProc];

        // sort processes by arrival time
        int[] indices = new int[numProc];
        for (int i = 0; i < numProc; i++) indices[i] = i;
        Arrays.sort(indices);

        int index = 0;
        while (index < numProc && ar_proc[indices[index]] <= time) {
            queue.add(indices[index]);
            inQueue[indices[index]] = true;
            index++;
        }

        while (!queue.isEmpty()) {
            int i = queue.poll();

            if (rem_bt[i] > timeQuantum) {
                time += timeQuantum;
                rem_bt[i] -= timeQuantum;
            } else {
                time += rem_bt[i];
                rem_bt[i] = 0;
                ct_proc[i] = time;
                isCompleted[i] = true;
                completed++;
            }

            // Enqueue new arrivals
            while (index < numProc && ar_proc[indices[index]] <= time) {
                if (!inQueue[indices[index]]) {
                    queue.add(indices[index]);
                    inQueue[indices[index]] = true;
                }
                index++;
            }

            // Re-queue the current process if it's not done
            if (rem_bt[i] > 0) {
                queue.add(i);
            }

            // If queue is empty but there are remaining processes, jump to next arrival
            if (queue.isEmpty() && completed < numProc) {
                if (index < numProc) {
                    time = Math.max(time, ar_proc[indices[index]]);
                    queue.add(indices[index]);
                    inQueue[indices[index]] = true;
                    index++;
                }
            }
        }

        // Calculate turnaround and waiting times
        double avg_wt = 0, avg_tat = 0;
        for (int i = 0; i < numProc; i++) {
            ta_proc[i] = ct_proc[i] - ar_proc[i];
            wt_proc[i] = ta_proc[i] - bt_proc[i];
            avg_wt += wt_proc[i];
            avg_tat += ta_proc[i];
        }

        // Display output
        System.out.println("Round Robin Scheduling (Time Quantum = " + timeQuantum + ")");
        System.out.println("Process\t\tAT\t\tBT\t\tCT\t\tTAT\t\tWT");
        for (int i = 0; i < numProc; i++) {
            System.out.println(pid_proc[i] + "\t\t\t" + ar_proc[i] + "\t\t" + bt_proc[i] + "\t\t" + ct_proc[i] + "\t\t" + ta_proc[i] + "\t\t" + wt_proc[i]);
        }

        System.out.println();
        System.out.println("Average Wait Time: " + (avg_wt / numProc));
        System.out.println("Average Turn Around Time: " + (avg_tat / numProc));
        printMetrics(numProc, ar_proc, bt_proc, ct_proc);
    }

    public static void ps(int numProc, int pid_proc[], int bt_proc[], int ar_proc[], int ct_proc[], int ta_proc[], int wt_proc[], int pr_proc[]) {
        boolean[] finished = new boolean[numProc];
        int[] original_pid = pid_proc.clone(); // To preserve original order
        int start_time = 0;
        int completed = 0;

        while (completed < numProc) {
            int idx = -1;
            int highestPriority = Integer.MAX_VALUE;

            for (int i = 0; i < numProc; i++) {
                if (!finished[i] && ar_proc[i] <= start_time && pr_proc[i] < highestPriority) {
                    highestPriority = pr_proc[i];
                    idx = i;
                }
            }

            if (idx == -1) {
                start_time++;
            } else {
                ct_proc[idx] = start_time + bt_proc[idx];
                ta_proc[idx] = ct_proc[idx] - ar_proc[idx];
                wt_proc[idx] = ta_proc[idx] - bt_proc[idx];
                start_time = ct_proc[idx];
                finished[idx] = true;
                completed++;
            }
        }

        double avg_wt = 0, avg_tat = 0;
        for (int i = 0; i < numProc; i++) {
            avg_wt += wt_proc[i];
            avg_tat += ta_proc[i];
        }

        System.out.println("Priority Scheduling");
        System.out.println("Process\t\tAT\t\tBT\t\tPriority\tCT\t\tTAT\t\tWT");
        for (int i = 0; i < numProc; i++) {
            System.out.println(original_pid[i] + "\t\t\t" + ar_proc[i] + "\t\t" + bt_proc[i] + "\t\t" + pr_proc[i] + "\t\t\t" + ct_proc[i] + "\t\t" + ta_proc[i] + "\t\t" + wt_proc[i]);
        }

        System.out.println();
        System.out.println("Average Wait Time: " + (avg_wt / numProc));
        System.out.println("Average Turn Around Time: " + (avg_tat / numProc));
        printMetrics(numProc, ar_proc, bt_proc, ct_proc);
    }

    public static void srtf(int numProc, int pid_proc[], int bt_proc[], int ar_proc[], int ct_proc[], int ta_proc[], int wt_proc[]) {
        int[] rem_bt = bt_proc.clone();
        int[] start_time = new int[numProc];
        Arrays.fill(start_time, -1);

        boolean[] finished = new boolean[numProc];
        int completed = 0, time = 0, prev = -1;

        while (completed < numProc) {
            int idx = -1;
            int min_bt = Integer.MAX_VALUE;

            for (int i = 0; i < numProc; i++) {
                if (ar_proc[i] <= time && !finished[i] && rem_bt[i] < min_bt && rem_bt[i] > 0) {
                    min_bt = rem_bt[i];
                    idx = i;
                }
            }

            if (idx == -1) {
                time++; // CPU is idle
                continue;
            }

            if (start_time[idx] == -1) start_time[idx] = time;

            rem_bt[idx]--;
            time++;

            if (rem_bt[idx] == 0) {
                ct_proc[idx] = time;
                ta_proc[idx] = ct_proc[idx] - ar_proc[idx];
                wt_proc[idx] = ta_proc[idx] - bt_proc[idx];
                finished[idx] = true;
                completed++;
            }
        }

        double avg_wt = 0, avg_tat = 0;
        for (int i = 0; i < numProc; i++) {
            avg_wt += wt_proc[i];
            avg_tat += ta_proc[i];
        }

        System.out.println("Shortest Remaining Time First");
        System.out.println("Process\t\tAT\t\tBT\t\tCT\t\tTAT\t\tWT");
        for (int i = 0; i < numProc; i++) {
            System.out.println(pid_proc[i] + "\t\t\t" + ar_proc[i] + "\t\t" + bt_proc[i] + "\t\t" + ct_proc[i] + "\t\t" + ta_proc[i] + "\t\t" + wt_proc[i]);
        }

        System.out.println();
        System.out.println("Average Wait Time: " + (avg_wt / numProc));
        System.out.println("Average Turn Around Time: " + (avg_tat / numProc));
        printMetrics(numProc, ar_proc, bt_proc, ct_proc);
    }
    public static void mlfq(int numProc, int pid_proc[], int bt_proc[], int ar_proc[], int ct_proc[], int ta_proc[], int wt_proc[]) {
        int[] rem_bt = bt_proc.clone();
        int[] arrival = ar_proc.clone();
        boolean[] completed = new boolean[numProc];

        int currentTime = 0;
        int completedCount = 0;

        Queue<Integer> q1 = new LinkedList<>();
        Queue<Integer> q2 = new LinkedList<>();
        Queue<Integer> q3 = new LinkedList<>();

        boolean[] inQueue = new boolean[numProc];

        // Initially, insert all processes that arrive at time 0
        for (int i = 0; i < numProc; i++) {
            if (arrival[i] == 0) {
                q1.add(i);
                inQueue[i] = true;
            }
        }

        while (completedCount < numProc) {
            // Check for new arrivals at current time
            for (int i = 0; i < numProc; i++) {
                if (arrival[i] <= currentTime && !inQueue[i] && !completed[i]) {
                    q1.add(i);
                    inQueue[i] = true;
                }
            }

            if (!q1.isEmpty()) {
                int idx = q1.poll();
                int execTime = Math.min(4, rem_bt[idx]);
                rem_bt[idx] -= execTime;
                currentTime += execTime;

                for (int i = 0; i < numProc; i++) {
                    if (arrival[i] <= currentTime && !inQueue[i] && !completed[i]) {
                        q1.add(i);
                        inQueue[i] = true;
                    }
                }

                if (rem_bt[idx] == 0) {
                    ct_proc[idx] = currentTime;
                    completed[idx] = true;
                    completedCount++;
                } else {
                    q2.add(idx);
                }
            } else if (!q2.isEmpty()) {
                int idx = q2.poll();
                int execTime = Math.min(8, rem_bt[idx]);
                rem_bt[idx] -= execTime;
                currentTime += execTime;

                for (int i = 0; i < numProc; i++) {
                    if (arrival[i] <= currentTime && !inQueue[i] && !completed[i]) {
                        q1.add(i);
                        inQueue[i] = true;
                    }
                }

                if (rem_bt[idx] == 0) {
                    ct_proc[idx] = currentTime;
                    completed[idx] = true;
                    completedCount++;
                } else {
                    q3.add(idx);
                }
            } else if (!q3.isEmpty()) {
                int idx = q3.poll();
                currentTime += rem_bt[idx];
                rem_bt[idx] = 0;
                ct_proc[idx] = currentTime;
                completed[idx] = true;
                completedCount++;

                for (int i = 0; i < numProc; i++) {
                    if (arrival[i] <= currentTime && !inQueue[i] && !completed[i]) {
                        q1.add(i);
                        inQueue[i] = true;
                    }
                }
            } else {
                currentTime++;
            }
        }

        double avg_wt = 0, avg_tat = 0;
        for (int i = 0; i < numProc; i++) {
            ta_proc[i] = ct_proc[i] - ar_proc[i];
            wt_proc[i] = ta_proc[i] - bt_proc[i];
            avg_wt += wt_proc[i];
            avg_tat += ta_proc[i];
        }

        System.out.println("Multilevel Feedback Queue Scheduling");
        System.out.println("Process\t\tAT\t\tBT\t\tCT\t\tTAT\t\tWT");
        for (int i = 0; i < numProc; i++) {
            System.out.println(pid_proc[i] + "\t\t\t" + ar_proc[i] + "\t\t" + bt_proc[i] + "\t\t" + ct_proc[i] + "\t\t" + ta_proc[i] + "\t\t" + wt_proc[i]);
        }

        System.out.println();
        System.out.println("Average Wait Time: " + (avg_wt / numProc));
        System.out.println("Average Turn Around Time: " + (avg_tat / numProc));
        printMetrics(numProc, ar_proc, bt_proc, ct_proc);
    }

    public static void printMetrics(int numProc, int[] ar, int[] bt, int[] ct) {
        int minArrival = Integer.MAX_VALUE;
        int maxCompletion = Integer.MIN_VALUE;
        int totalBurst = 0;
        for (int i = 0; i < numProc; i++) {
            minArrival = Math.min(minArrival, ar[i]);
            maxCompletion = Math.max(maxCompletion, ct[i]);
            totalBurst += bt[i];
        }
        double totalTime = maxCompletion - minArrival;
        double throughput = numProc / totalTime;
        double cpuUtil = (totalBurst / totalTime) * 100.0;

        System.out.printf("Throughput: %.2f processes/unit time%n", throughput);
        System.out.printf("CPU Utilization: %.2f%%%n", cpuUtil);
    }


    public static void main(String[] args) {
        System.out.println("Enter the number of processes:");
        Scanner input = new Scanner(System.in);
        int numberOfProcesses = input.nextInt();

        int pid[] = new int[numberOfProcesses]; //process id number
        int bt[] = new int[numberOfProcesses];  //burst time
        int ar[] = new int[numberOfProcesses];  //arrival time
        int ct[] = new int[numberOfProcesses];  //completion time
        int ta[] = new int[numberOfProcesses];  //turn around time
        int wt[] = new int[numberOfProcesses];  //wait time
        int pr[] = new int[numberOfProcesses]; // priority array

        for (int i = 0; i < numberOfProcesses; i++){
            System.out.println("Enter the arrival time of process " + (i+1) + ": ");
            ar[i] = input.nextInt();
            System.out.println("Enter the burst time of process " + (i+1) + ": ");
            bt[i] = input.nextInt();
            pid[i] = i + 1;
            System.out.println("Enter the priority of process " + (i + 1) + ": (Lower number = higher priority)");
            pr[i] = input.nextInt();
            System.out.println();
        }
        System.out.println("Enter the time quantum for Round Robin:");
        int tq = input.nextInt();
        System.out.println();

        //Start running each scheduling method
        fcfs(numberOfProcesses, pid.clone(), bt.clone(), ar.clone(), ct.clone(), ta.clone(), wt.clone()); //First Come First Serve
        System.out.println("\n\n");
        sjf(numberOfProcesses, pid.clone(), bt.clone(), ar.clone(), ct.clone(), ta.clone(), wt.clone());    //Shortest Job First
        System.out.println("\n\n");
        rr(numberOfProcesses, pid.clone(), bt.clone(), ar.clone(), ct.clone(), ta.clone(), wt.clone(), tq); //Round Robin
        System.out.println("\n\n");
        ps(numberOfProcesses, pid.clone(), bt.clone(), ar.clone(), ct.clone(), ta.clone(), wt.clone(), pr.clone()); //Priority Scheduling
        System.out.println("\n\n");
        srtf(numberOfProcesses, pid.clone(), bt.clone(), ar.clone(), ct.clone(), ta.clone(), wt.clone());   //Shortest Remaining Job First
        System.out.println("\n\n");
        mlfq(numberOfProcesses, pid.clone(), bt.clone(), ar.clone(), ct.clone(), ta.clone(), wt.clone());   //Multilevel Feedback Queue

    }
}
